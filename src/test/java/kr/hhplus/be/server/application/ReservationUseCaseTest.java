package kr.hhplus.be.server.application;

import kr.hhplus.be.server.application.dto.ReservationResult;
import kr.hhplus.be.server.application.usecase.reservation.ReservationUseCase;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueStatus;
import kr.hhplus.be.server.domain.reservation.*;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("예약 UseCase 테스트")
class ReservationUseCaseTest {

    @Mock
    private SeatRepository seatRepository;
    
    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private QueueTokenRepository queueTokenRepository;

    private ReservationUseCase reservationUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reservationUseCase = new ReservationUseCase(seatRepository, reservationRepository, queueTokenRepository);
    }

    @Test
    @DisplayName("좌석을 예약할 수 있다")
    void reserveSeat() {
        String userId = "user123";
        Long seatId = 1L;
        
        QueueToken queueToken = new QueueToken(1L, userId, "token", 1, QueueStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
        Seat seat = Seat.create(1L, 1, 50000);
        Seat seatWithId = new Seat(1L, seat.getConcertScheduleId(), seat.getSeatNumber(), 
                                  seat.getPrice(), seat.getStatus(), seat.getReservedBy(),
                                  seat.getReservedAt());
        
        when(queueTokenRepository.findActiveByUserId(userId)).thenReturn(Optional.of(queueToken));
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seatWithId));
        when(reservationRepository.save(any(Reservation.class)))
            .thenAnswer(invocation -> {
                Reservation reservation = invocation.getArgument(0);
                return new Reservation(1L, reservation.getUserId(), reservation.getSeatId(), 
                                     reservation.getPrice(), reservation.getStatus(), 
                                     reservation.getCreatedAt());
            });

        ReservationResult result = reservationUseCase.reserve(userId, seatId);

        assertThat(result.getReservationId()).isEqualTo(1L);
        assertThat(result.getPrice()).isEqualTo(50000);
        assertThat(result.getExpiresAt()).isEqualTo(queueToken.getExpiresAt());
        
        verify(seatRepository).save(any(Seat.class));
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("존재하지 않는 좌석은 예약할 수 없다")
    void cannotReserveNonExistentSeat() {
        String userId = "user123";
        Long seatId = 1L;
        
        QueueToken queueToken = new QueueToken(1L, userId, "token", 1, QueueStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
        
        when(queueTokenRepository.findActiveByUserId(userId)).thenReturn(Optional.of(queueToken));
        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationUseCase.reserve(userId, seatId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 좌석입니다.");
    }

    @Test
    @DisplayName("활성화된 QueueToken이 없으면 예약할 수 없다")
    void cannotReserveWithoutActiveQueueToken() {
        String userId = "user123";
        Long seatId = 1L;
        
        when(queueTokenRepository.findActiveByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationUseCase.reserve(userId, seatId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("활성화된 대기열 토큰이 없습니다.");
        
        verify(seatRepository, never()).findById(any());
    }

    @Test
    @DisplayName("QueueToken이 만료되면 예약할 수 없다")
    void cannotReserveWithExpiredQueueToken() {
        String userId = "user123";
        Long seatId = 1L;
        
        QueueToken expiredToken = new QueueToken(1L, userId, "token", 1, QueueStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().minusMinutes(1)); // 1분 전에 만료
        
        when(queueTokenRepository.findActiveByUserId(userId)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> reservationUseCase.reserve(userId, seatId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("대기열 토큰이 만료되었습니다.");
        
        verify(queueTokenRepository).save(expiredToken);
        verify(seatRepository, never()).findById(any());
    }

    @Test
    @DisplayName("만료된 선점자의 좌석을 다른 사용자가 예약할 수 있다")
    void canReserveSeatFromExpiredReserver() {
        String currentUser = "user123";
        String previousReserver = "user456";
        Long seatId = 1L;
        
        QueueToken currentUserToken = new QueueToken(1L, currentUser, "token1", 1, QueueStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
        QueueToken expiredReserverToken = new QueueToken(2L, previousReserver, "token2", 2, QueueStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().minusMinutes(1)); // 만료됨
        
        Seat temporarilyReservedSeat = new Seat(1L, 1L, 1, 50000, SeatStatus.TEMPORARILY_RESERVED, 
                                               previousReserver, LocalDateTime.now());
        
        when(queueTokenRepository.findActiveByUserId(currentUser)).thenReturn(Optional.of(currentUserToken));
        when(queueTokenRepository.findActiveByUserId(previousReserver)).thenReturn(Optional.of(expiredReserverToken));
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(temporarilyReservedSeat));
        when(reservationRepository.save(any(Reservation.class)))
            .thenAnswer(invocation -> {
                Reservation reservation = invocation.getArgument(0);
                return new Reservation(1L, reservation.getUserId(), reservation.getSeatId(), 
                                     reservation.getPrice(), reservation.getStatus(), 
                                     reservation.getCreatedAt());
            });

        ReservationResult result = reservationUseCase.reserve(currentUser, seatId);

        assertThat(result.getReservationId()).isEqualTo(1L);
        assertThat(result.getPrice()).isEqualTo(50000);
        
        verify(queueTokenRepository).save(expiredReserverToken);
        verify(seatRepository).save(any(Seat.class));
        verify(reservationRepository).save(any(Reservation.class));
    }
}

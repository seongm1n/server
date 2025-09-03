package kr.hhplus.be.server.application;

import kr.hhplus.be.server.application.dto.ReservationResult;
import kr.hhplus.be.server.application.usecase.reservation.ReservationUseCase;
import kr.hhplus.be.server.domain.reservation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    private ReservationUseCase reservationUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reservationUseCase = new ReservationUseCase(seatRepository, reservationRepository);
    }

    @Test
    @DisplayName("좌석을 예약할 수 있다")
    void reserveSeat() {
        String userId = "user123";
        Long seatId = 1L;
        
        Seat seat = Seat.create(1L, 1, 50000);
        Seat seatWithId = new Seat(1L, seat.getConcertScheduleId(), seat.getSeatNumber(), 
                                  seat.getPrice(), seat.getStatus(), seat.getReservedBy(),
                                  seat.getReservedAt(), seat.getExpiresAt());
        
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seatWithId));
        when(reservationRepository.save(any(Reservation.class)))
            .thenAnswer(invocation -> {
                Reservation reservation = invocation.getArgument(0);
                return new Reservation(1L, reservation.getUserId(), reservation.getSeatId(), 
                                     reservation.getPrice(), reservation.getStatus(), 
                                     reservation.getCreatedAt(), reservation.getExpiresAt());
            });

        ReservationResult result = reservationUseCase.reserve(userId, seatId);

        assertThat(result.getReservationId()).isEqualTo(1L);
        assertThat(result.getPrice()).isEqualTo(50000);
        assertThat(result.getExpiresAt()).isNotNull();
        
        verify(seatRepository).save(any(Seat.class));
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("존재하지 않는 좌석은 예약할 수 없다")
    void cannotReserveNonExistentSeat() {
        String userId = "user123";
        Long seatId = 1L;
        
        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationUseCase.reserve(userId, seatId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 좌석입니다.");
    }
}

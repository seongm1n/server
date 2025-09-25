package kr.hhplus.be.server.application;

import kr.hhplus.be.server.application.dto.SeatResult;
import kr.hhplus.be.server.application.usecase.seat.SeatUseCase;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("좌석 UseCase 테스트")
class SeatUseCaseTest {

    @Mock
    private SeatRepository seatRepository;

    private SeatUseCase seatUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        seatUseCase = new SeatUseCase(seatRepository);
    }

    @Test
    @DisplayName("콘서트 스케줄의 예약 가능한 좌석을 조회할 수 있다")
    void getAvailableSeats() {
        Long concertScheduleId = 1L;
        List<Seat> availableSeats = List.of(
            new Seat(1L, concertScheduleId, 1, 50000, SeatStatus.AVAILABLE, null, null),
            new Seat(2L, concertScheduleId, 2, 50000, SeatStatus.AVAILABLE, null, null),
            new Seat(5L, concertScheduleId, 5, 50000, SeatStatus.AVAILABLE, null, null)
        );

        when(seatRepository.findAvailableSeatsByConcertScheduleId(concertScheduleId))
            .thenReturn(availableSeats);

        List<SeatResult> results = seatUseCase.getAvailableSeats(concertScheduleId);

        assertThat(results).hasSize(3);
        assertThat(results)
            .extracting(SeatResult::getSeatNumber)
            .containsExactly(1, 2, 5);
        assertThat(results)
            .allMatch(result -> result.getStatus() == SeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("특정 좌석을 조회할 수 있다")
    void getSeat() {
        Long seatId = 1L;
        Seat seat = new Seat(seatId, 1L, 1, 50000, SeatStatus.AVAILABLE, null, null);

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

        SeatResult result = seatUseCase.getSeat(seatId);

        assertThat(result.getId()).isEqualTo(seatId);
        assertThat(result.getSeatNumber()).isEqualTo(1);
        assertThat(result.getPrice()).isEqualTo(50000);
        assertThat(result.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("존재하지 않는 좌석 조회 시 예외가 발생한다")
    void getSeatNotFound() {
        Long seatId = 999L;

        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatUseCase.getSeat(seatId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("좌석을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("좌석을 예약할 수 있다")
    void reserveSeat() {
        Long seatId = 1L;
        String userId = "user123";
        Seat seat = new Seat(seatId, 1L, 1, 50000, SeatStatus.AVAILABLE, null, null);
        
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any(Seat.class))).thenAnswer(invocation -> {
            Seat savedSeat = invocation.getArgument(0);
            return new Seat(savedSeat.getId(), savedSeat.getConcertScheduleId(), 
                          savedSeat.getSeatNumber(), savedSeat.getPrice(), 
                          savedSeat.getStatus(), savedSeat.getReservedBy(), 
                          savedSeat.getReservedAt());
        });

        SeatResult result = seatUseCase.reserveSeat(seatId, userId);

        assertThat(result.getStatus()).isEqualTo(SeatStatus.TEMPORARILY_RESERVED);
        assertThat(result.getReservedBy()).isEqualTo(userId);
        assertThat(result.getReservedAt()).isNotNull();
        
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    @DisplayName("존재하지 않는 좌석 예약 시 예외가 발생한다")
    void reserveSeatNotFound() {
        Long seatId = 999L;
        String userId = "user123";

        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatUseCase.reserveSeat(seatId, userId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("좌석을 찾을 수 없습니다.");
            
        verify(seatRepository, never()).save(any());
    }

    @Test
    @DisplayName("좌석 예약을 확정할 수 있다")
    void confirmSeat() {
        Long seatId = 1L;
        String userId = "user123";
        Seat seat = new Seat(seatId, 1L, 1, 50000, SeatStatus.TEMPORARILY_RESERVED, 
                           userId, LocalDateTime.now());

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any(Seat.class))).thenAnswer(invocation -> {
            Seat savedSeat = invocation.getArgument(0);
            return new Seat(savedSeat.getId(), savedSeat.getConcertScheduleId(), 
                          savedSeat.getSeatNumber(), savedSeat.getPrice(), 
                          SeatStatus.CONFIRMED, savedSeat.getReservedBy(), 
                          savedSeat.getReservedAt());
        });

        SeatResult result = seatUseCase.confirmSeat(seatId);

        assertThat(result.getStatus()).isEqualTo(SeatStatus.CONFIRMED);
        assertThat(result.getReservedBy()).isEqualTo(userId);
        
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    @DisplayName("좌석 예약을 해제할 수 있다")
    void releaseSeat() {
        Long seatId = 1L;
        String userId = "user123";
        Seat seat = new Seat(seatId, 1L, 1, 50000, SeatStatus.TEMPORARILY_RESERVED, 
                           userId, LocalDateTime.now());

        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any(Seat.class))).thenAnswer(invocation -> {
            Seat savedSeat = invocation.getArgument(0);
            return new Seat(savedSeat.getId(), savedSeat.getConcertScheduleId(), 
                          savedSeat.getSeatNumber(), savedSeat.getPrice(), 
                          SeatStatus.AVAILABLE, null, null);
        });

        SeatResult result = seatUseCase.releaseSeat(seatId);

        assertThat(result.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(result.getReservedBy()).isNull();
        assertThat(result.getReservedAt()).isNull();
        
        verify(seatRepository).save(any(Seat.class));
    }

    @Test
    @DisplayName("존재하지 않는 좌석 확정 시 예외가 발생한다")
    void confirmSeatNotFound() {
        Long seatId = 999L;

        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatUseCase.confirmSeat(seatId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("좌석을 찾을 수 없습니다.");
            
        verify(seatRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 좌석 해제 시 예외가 발생한다")
    void releaseSeatNotFound() {
        Long seatId = 999L;

        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seatUseCase.releaseSeat(seatId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("좌석을 찾을 수 없습니다.");
            
        verify(seatRepository, never()).save(any());
    }
}

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
    @DisplayName("만료된 임시 예약을 해제할 수 있다")
    void expireTemporaryReservations() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(5);
        List<Seat> expiredSeats = List.of(
            new Seat(1L, 1L, 1, 50000, SeatStatus.TEMPORARILY_RESERVED, "user123", expirationTime.minusMinutes(1)),
            new Seat(2L, 1L, 2, 50000, SeatStatus.TEMPORARILY_RESERVED, "user456", expirationTime.minusMinutes(2))
        );

        when(seatRepository.findExpiredTemporaryReservations(any(LocalDateTime.class)))
            .thenReturn(expiredSeats);

        seatUseCase.expireTemporaryReservations();

        verify(seatRepository).findExpiredTemporaryReservations(any(LocalDateTime.class));
        verify(seatRepository, times(2)).save(any(Seat.class));
    }
}

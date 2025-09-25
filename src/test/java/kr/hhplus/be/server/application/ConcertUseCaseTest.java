package kr.hhplus.be.server.application;

import kr.hhplus.be.server.application.usecase.concert.ConcertUseCase;
import kr.hhplus.be.server.domain.concert.*;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("콘서트 UseCase 테스트")
class ConcertUseCaseTest {

    @Mock
    private ConcertRepository concertRepository;
    
    @Mock
    private SeatRepository seatRepository;

    private ConcertUseCase concertUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        concertUseCase = new ConcertUseCase(concertRepository, seatRepository);
    }

    @Test
    @DisplayName("예약 가능한 날짜를 조회할 수 있다")
    void getAvailableDates() {
        Long concertId = 1L;
        
        when(concertRepository.getAvailableDates(concertId)).thenReturn(
            List.of(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 2))
        );

        List<LocalDate> dates = concertUseCase.getAvailableDates(concertId);

        assertThat(dates).hasSize(2);
        assertThat(dates).contains(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 2));
    }

    @Test
    @DisplayName("예약 가능한 좌석을 조회할 수 있다")
    void getAvailableSeats() {
        Long concertScheduleId = 1L;
        
        List<Seat> availableSeats = List.of(
            Seat.create(concertScheduleId, 1, 50000),
            Seat.create(concertScheduleId, 5, 50000),
            Seat.create(concertScheduleId, 10, 50000)
        );
        
        when(seatRepository.findAvailableSeatsByConcertScheduleId(concertScheduleId))
            .thenReturn(availableSeats);

        List<Integer> seatNumbers = concertUseCase.getAvailableSeats(concertScheduleId);

        assertThat(seatNumbers).hasSize(3);
        assertThat(seatNumbers).contains(1, 5, 10);
    }
}

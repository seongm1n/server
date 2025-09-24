package kr.hhplus.be.server.infrastructure.persistence.seat;

import kr.hhplus.be.server.domain.reservation.Seat;
import kr.hhplus.be.server.domain.reservation.SeatStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SeatRepositoryImpl.class)
@ActiveProfiles("test")
class SeatRepositoryImplTest {

    @Autowired
    private SeatRepositoryImpl repository;
    
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void 좌석_ID로_조회() {
        Long scheduleId = createConcertSchedule();
        
        Seat seat = Seat.create(scheduleId, 1, 50000);
        Seat saved = repository.save(seat);

        Optional<Seat> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getConcertScheduleId()).isEqualTo(scheduleId);
        assertThat(found.get().getSeatNumber()).isEqualTo(1);
        assertThat(found.get().getPrice()).isEqualTo(50000);
        assertThat(found.get().getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void 존재하지_않는_좌석_조회() {
        Optional<Seat> found = repository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void 좌석_저장_및_상태_변경() {
        Long scheduleId = createConcertSchedule();
        
        Seat seat = Seat.create(scheduleId, 1, 50000);
        Seat saved = repository.save(seat);

        saved.reserve("user1");
        Seat updated = repository.save(saved);

        assertThat(updated.getStatus()).isEqualTo(SeatStatus.TEMPORARILY_RESERVED);
        assertThat(updated.getReservedBy()).isEqualTo("user1");
        assertThat(updated.getReservedAt()).isNotNull();
    }

    @Test
    void 스케줄_ID로_사용가능한_좌석_조회() {
        Long scheduleId1 = createConcertSchedule();
        Long scheduleId2 = createConcertSchedule();
        
        Seat seat1 = Seat.create(scheduleId1, 1, 50000);
        Seat seat2 = Seat.create(scheduleId1, 2, 50000);
        Seat seat3 = Seat.create(scheduleId1, 3, 50000);
        Seat seat4 = Seat.create(scheduleId2, 1, 60000);

        repository.save(seat1);
        repository.save(seat2);
        Seat saved3 = repository.save(seat3);
        repository.save(seat4);

        saved3.reserve("user1");
        repository.save(saved3);

        List<Seat> availableSeats = repository.findAvailableSeatsByScheduleId(scheduleId1);

        assertThat(availableSeats).hasSize(2);
        assertThat(availableSeats)
                .extracting(Seat::getSeatNumber)
                .containsExactlyInAnyOrder(1, 2);
        assertThat(availableSeats)
                .allMatch(seat -> seat.getStatus() == SeatStatus.AVAILABLE);
    }

    @Test
    void 다른_스케줄의_좌석은_조회되지_않음() {
        Long scheduleId1 = createConcertSchedule();
        Long scheduleId2 = createConcertSchedule();
        
        Seat seat1 = Seat.create(scheduleId1, 1, 50000);
        Seat seat2 = Seat.create(scheduleId2, 1, 60000);

        repository.save(seat1);
        repository.save(seat2);

        List<Seat> schedule1Seats = repository.findAvailableSeatsByScheduleId(scheduleId1);
        List<Seat> schedule2Seats = repository.findAvailableSeatsByScheduleId(scheduleId2);

        assertThat(schedule1Seats).hasSize(1);
        assertThat(schedule1Seats.get(0).getConcertScheduleId()).isEqualTo(scheduleId1);
        
        assertThat(schedule2Seats).hasSize(1);
        assertThat(schedule2Seats.get(0).getConcertScheduleId()).isEqualTo(scheduleId2);
    }

    @Test
    void 사용가능한_좌석이_없는_경우() {
        Long scheduleId = createConcertSchedule();
        
        Seat seat1 = Seat.create(scheduleId, 1, 50000);
        Seat seat2 = Seat.create(scheduleId, 2, 50000);

        Seat saved1 = repository.save(seat1);
        Seat saved2 = repository.save(seat2);

        saved1.reserve("user1");
        saved2.reserve("user2");
        repository.save(saved1);
        repository.save(saved2);

        List<Seat> availableSeats = repository.findAvailableSeatsByScheduleId(scheduleId);

        assertThat(availableSeats).isEmpty();
    }
    
    private Long createConcertSchedule() {
        entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO concert (name) VALUES ('테스트 콘서트')")
                .executeUpdate();
        
        entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO concert_schedule (concert_id, concert_date, start_time, end_time, total_seats) VALUES (1, '2024-03-01', '19:00:00', '21:00:00', 50)")
                .executeUpdate();
        
        entityManager.flush();
        
        return (Long) entityManager.getEntityManager()
                .createNativeQuery("SELECT LAST_INSERT_ID()")
                .getSingleResult();
    }
}
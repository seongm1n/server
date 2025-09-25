package kr.hhplus.be.server.infrastructure.persistence.concert;

import kr.hhplus.be.server.infrastructure.persistence.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("콘서트 Repository 구현체 테스트")
class ConcertRepositoryImplTest extends TestContainerConfig {

    @Autowired
    private ConcertScheduleJpaRepository concertScheduleJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ConcertRepositoryImpl concertRepository;

    @BeforeEach
    void setUp() {
        concertRepository = new ConcertRepositoryImpl(concertScheduleJpaRepository);
    }

    @Test
    @DisplayName("콘서트 ID로 예약 가능한 날짜를 조회할 수 있다")
    void getAvailableDates() {
        // given
        Long concertId = createConcert("테스트 콘서트");
        createConcertSchedule(concertId, LocalDate.of(2024, 3, 1), LocalTime.of(19, 0), LocalTime.of(21, 0), 100);
        createConcertSchedule(concertId, LocalDate.of(2024, 3, 2), LocalTime.of(19, 0), LocalTime.of(21, 0), 100);
        createConcertSchedule(concertId, LocalDate.of(2024, 3, 3), LocalTime.of(19, 0), LocalTime.of(21, 0), 100);

        // when
        List<LocalDate> availableDates = concertRepository.getAvailableDates(concertId);

        // then
        assertThat(availableDates).hasSize(3);
        assertThat(availableDates).containsExactlyInAnyOrder(
            LocalDate.of(2024, 3, 1),
            LocalDate.of(2024, 3, 2),
            LocalDate.of(2024, 3, 3)
        );
    }

    @Test
    @DisplayName("같은 날짜의 여러 스케줄이 있어도 중복 제거되어 조회된다")
    void getAvailableDatesWithDuplicates() {
        // given
        Long concertId = createConcert("테스트 콘서트");
        createConcertSchedule(concertId, LocalDate.of(2024, 3, 1), LocalTime.of(14, 0), LocalTime.of(16, 0), 100);
        createConcertSchedule(concertId, LocalDate.of(2024, 3, 1), LocalTime.of(19, 0), LocalTime.of(21, 0), 100);
        createConcertSchedule(concertId, LocalDate.of(2024, 3, 2), LocalTime.of(19, 0), LocalTime.of(21, 0), 100);

        // when
        List<LocalDate> availableDates = concertRepository.getAvailableDates(concertId);

        // then
        assertThat(availableDates).hasSize(2);
        assertThat(availableDates).containsExactlyInAnyOrder(
            LocalDate.of(2024, 3, 1),
            LocalDate.of(2024, 3, 2)
        );
    }

    @Test
    @DisplayName("다른 콘서트의 스케줄은 조회되지 않는다")
    void getAvailableDatesFiltersByConcertId() {
        // given
        Long concertId1 = createConcert("콘서트 1");
        Long concertId2 = createConcert("콘서트 2");
        
        createConcertSchedule(concertId1, LocalDate.of(2024, 3, 1), LocalTime.of(19, 0), LocalTime.of(21, 0), 100);
        createConcertSchedule(concertId1, LocalDate.of(2024, 3, 2), LocalTime.of(19, 0), LocalTime.of(21, 0), 100);
        createConcertSchedule(concertId2, LocalDate.of(2024, 3, 3), LocalTime.of(19, 0), LocalTime.of(21, 0), 100);

        // when
        List<LocalDate> concert1Dates = concertRepository.getAvailableDates(concertId1);
        List<LocalDate> concert2Dates = concertRepository.getAvailableDates(concertId2);

        // then
        assertThat(concert1Dates).hasSize(2);
        assertThat(concert1Dates).containsExactlyInAnyOrder(
            LocalDate.of(2024, 3, 1),
            LocalDate.of(2024, 3, 2)
        );
        
        assertThat(concert2Dates).hasSize(1);
        assertThat(concert2Dates).containsExactly(LocalDate.of(2024, 3, 3));
    }

    @Test
    @DisplayName("스케줄이 없는 콘서트는 빈 목록을 반환한다")
    void getAvailableDatesReturnsEmptyForConcertWithNoSchedules() {
        // given
        Long concertId = createConcert("스케줄 없는 콘서트");

        // when
        List<LocalDate> availableDates = concertRepository.getAvailableDates(concertId);

        // then
        assertThat(availableDates).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 콘서트 ID로 조회하면 빈 목록을 반환한다")
    void getAvailableDatesReturnsEmptyForNonExistentConcert() {
        // given
        Long nonExistentConcertId = 999L;

        // when
        List<LocalDate> availableDates = concertRepository.getAvailableDates(nonExistentConcertId);

        // then
        assertThat(availableDates).isEmpty();
    }

    private Long createConcert(String name) {
        entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO concert (name) VALUES (?)")
                .setParameter(1, name)
                .executeUpdate();
        
        entityManager.flush();
        
        return (Long) entityManager.getEntityManager()
                .createNativeQuery("SELECT LAST_INSERT_ID()")
                .getSingleResult();
    }

    private Long createConcertSchedule(Long concertId, LocalDate concertDate, LocalTime startTime, LocalTime endTime, int totalSeats) {
        entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO concert_schedule (concert_id, concert_date, start_time, end_time, total_seats) VALUES (?, ?, ?, ?, ?)")
                .setParameter(1, concertId)
                .setParameter(2, concertDate)
                .setParameter(3, startTime)
                .setParameter(4, endTime)
                .setParameter(5, totalSeats)
                .executeUpdate();
        
        entityManager.flush();
        
        return (Long) entityManager.getEntityManager()
                .createNativeQuery("SELECT LAST_INSERT_ID()")
                .getSingleResult();
    }
}

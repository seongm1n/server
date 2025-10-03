package kr.hhplus.be.server.infrastructure.persistence.reservation;

import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import kr.hhplus.be.server.infrastructure.persistence.reservation.ReservationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReservationRepositoryImplTest extends kr.hhplus.be.server.infrastructure.persistence.TestContainerConfig {

    @Autowired
    private ReservationJpaRepository jpaRepository;
    
    private ReservationRepositoryImpl repository;
    
    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository = new ReservationRepositoryImpl(jpaRepository);
    }

    @Test
    void 예약_생성_저장() {
        Long seatId = createSeat();
        
        Reservation reservation = Reservation.create("user1", seatId, 50000);
        Reservation saved = repository.save(reservation);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo("user1");
        assertThat(saved.getSeatId()).isEqualTo(seatId);
        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void 예약_ID로_조회() {
        Long seatId = createSeat();
        
        Reservation reservation = Reservation.create("user1", seatId, 50000);
        Reservation saved = repository.save(reservation);

        Optional<Reservation> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getUserId()).isEqualTo("user1");
        assertThat(found.get().getSeatId()).isEqualTo(seatId);
        assertThat(found.get().getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void 존재하지_않는_예약_조회() {
        Optional<Reservation> found = repository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void 예약_상태_변경_저장() {
        Long seatId = createSeat();
        
        Reservation reservation = Reservation.create("user1", seatId, 50000);
        Reservation saved = repository.save(reservation);

        saved.confirm();
        Reservation updated = repository.save(saved);

        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(updated.getId()).isEqualTo(saved.getId());
    }

    @Test
    void 예약_취소_상태_변경() {
        Long seatId = createSeat();
        
        Reservation reservation = Reservation.create("user1", seatId, 50000);
        Reservation saved = repository.save(reservation);

        saved.cancel();
        Reservation updated = repository.save(saved);

        assertThat(updated.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(updated.getId()).isEqualTo(saved.getId());
    }
    
    private Long createSeat() {
        entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO seat (concert_schedule_id, seat_number, price, status) VALUES (1, 1, 50000, 'AVAILABLE')")
                .executeUpdate();
        
        entityManager.flush();
        
        return (Long) entityManager.getEntityManager()
                .createNativeQuery("SELECT LAST_INSERT_ID()")
                .getSingleResult();
    }
}

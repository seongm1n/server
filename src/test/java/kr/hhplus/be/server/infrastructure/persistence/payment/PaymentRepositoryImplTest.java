package kr.hhplus.be.server.infrastructure.persistence.payment;

import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import kr.hhplus.be.server.infrastructure.persistence.payment.PaymentJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PaymentRepositoryImplTest extends kr.hhplus.be.server.infrastructure.persistence.TestContainerConfig {

    @Autowired
    private PaymentJpaRepository jpaRepository;
    
    private PaymentRepositoryImpl repository;
    
    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        repository = new PaymentRepositoryImpl(jpaRepository);
    }

    @Test
    void 결제_생성_저장() {
        Long reservationId = createReservation();
        
        Payment payment = Payment.create("user1", reservationId, 50000);
        Payment saved = repository.save(payment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo("user1");
        assertThat(saved.getReservationId()).isEqualTo(reservationId);
        assertThat(saved.getAmount()).isEqualTo(50000);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getCompletedAt()).isNull();
        assertThat(saved.getFailureReason()).isNull();
    }

    @Test
    void 결제_완료_상태_업데이트() {
        Long reservationId = createReservation();
        
        Payment payment = Payment.create("user1", reservationId, 50000);
        Payment saved = repository.save(payment);

        saved.complete();
        Payment updated = repository.save(saved);

        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(updated.getCompletedAt()).isNotNull();
    }

    @Test
    void 기존_결제_정보_업데이트() {
        Long reservationId = createReservation();
        
        Payment payment = Payment.create("user1", reservationId, 50000);
        Payment saved = repository.save(payment);

        Payment found = new Payment(
                saved.getId(),
                saved.getUserId(),
                saved.getReservationId(),
                saved.getAmount(),
                PaymentStatus.COMPLETED,
                saved.getCreatedAt(),
                saved.getCompletedAt(),
                saved.getFailureReason()
        );
        
        Payment updated = repository.save(found);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }
    
    private Long createReservation() {
        // seat 먼저 생성
        entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO seat (concert_schedule_id, seat_number, price, status) VALUES (1, 1, 50000, 'AVAILABLE')")
                .executeUpdate();
        
        Long seatId = (Long) entityManager.getEntityManager()
                .createNativeQuery("SELECT LAST_INSERT_ID()")
                .getSingleResult();
        
        // reservation 생성
        entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO reservation (user_id, seat_id, status, reserved_at, expires_at) VALUES ('user1', ?, 'CONFIRMED', NOW(), DATE_ADD(NOW(), INTERVAL 10 MINUTE))")
                .setParameter(1, seatId)
                .executeUpdate();
        
        entityManager.flush();
        
        return (Long) entityManager.getEntityManager()
                .createNativeQuery("SELECT LAST_INSERT_ID()")
                .getSingleResult();
    }
}

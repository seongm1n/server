package kr.hhplus.be.server.integration;

import kr.hhplus.be.server.application.dto.ReservationResult;
import kr.hhplus.be.server.application.usecase.balance.BalanceUseCase;
import kr.hhplus.be.server.application.usecase.payment.PaymentUseCase;
import kr.hhplus.be.server.application.usecase.reservation.ReservationUseCase;
import kr.hhplus.be.server.domain.queue.QueueStatus;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import kr.hhplus.be.server.domain.user.UserBalance;
import kr.hhplus.be.server.domain.user.UserBalanceRepository;
import kr.hhplus.be.server.infrastructure.persistence.TestContainerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DistributedLockIntegrationTest extends TestContainerConfig {

    @Autowired
    private ReservationUseCase reservationUseCase;

    @Autowired
    private PaymentUseCase paymentUseCase;

    @Autowired
    private BalanceUseCase balanceUseCase;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private QueueTokenRepository queueTokenRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Test
    @DisplayName("[분산락] 좌석 중복 예약 방지 - 10 threads → 1 success")
    void preventDuplicateSeatReservation() throws InterruptedException {
        long concertScheduleId = System.currentTimeMillis();
        Seat seat = Seat.create(concertScheduleId, 1, 50000);
        Long seatId = seatRepository.save(seat).getId();

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            String userId = "user" + System.currentTimeMillis() + "_" + i;
            createActiveQueueToken(userId);

            executorService.submit(() -> {
                try {
                    reservationUseCase.reserve(userId, seatId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);

        Seat result = seatRepository.findById(seatId).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(SeatStatus.TEMPORARILY_RESERVED);
    }

    @Test
    @DisplayName("[분산락] 잔액 동시 차감 - 음수 방지")
    void preventNegativeBalance() throws InterruptedException {
        String userId = "balance-user-" + System.currentTimeMillis();
        UserBalance userBalance = UserBalance.create(userId, 10000);
        userBalanceRepository.save(userBalance);

        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    balanceUseCase.use(userId, 3000);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                }
                finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(successCount.get()).isEqualTo(3);

        UserBalance result = userBalanceRepository.findByUserId(userId).orElseThrow();
        assertThat(result.getBalance()).isEqualTo(1000);
        assertThat(result.getBalance()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("[분산락] 결제 중복 처리 방지")
    void preventDuplicatePayment() throws InterruptedException {
        String userId = "payment-user-" + System.currentTimeMillis();

        UserBalance userBalance = UserBalance.create(userId, 100000);
        userBalanceRepository.save(userBalance);

        createActiveQueueToken(userId);

        long concertScheduleId = System.currentTimeMillis();
        Seat seat = Seat.create(concertScheduleId, 1, 50000);
        Long seatId = seatRepository.save(seat).getId();

        ReservationResult reservation = reservationUseCase.reserve(userId, seatId);
        Long reservationId = reservation.getReservationId();

        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    paymentUseCase.pay(userId, reservationId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(successCount.get()).isEqualTo(1);

        UserBalance result = userBalanceRepository.findByUserId(userId).orElseThrow();
        assertThat(result.getBalance()).isEqualTo(50000);
    }

    private void createActiveQueueToken(String userId) {
        QueueToken token = new QueueToken(
            null,
            userId,
            "token-" + userId,
            0,
            QueueStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now().plusMinutes(10)
        );
        queueTokenRepository.save(token);
    }
}

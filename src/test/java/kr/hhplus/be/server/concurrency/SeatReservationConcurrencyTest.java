package kr.hhplus.be.server.concurrency;

import kr.hhplus.be.server.application.usecase.reservation.ReservationUseCase;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.queue.QueueStatus;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import kr.hhplus.be.server.infrastructure.persistence.TestContainerConfig;
import org.junit.jupiter.api.AfterEach;
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
class SeatReservationConcurrencyTest extends TestContainerConfig {

    @Autowired
    private ReservationUseCase reservationUseCase;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private QueueTokenRepository queueTokenRepository;

    private Long testSeatId;

    @AfterEach
    void tearDown() {
        if (testSeatId != null) {
            seatRepository.findById(testSeatId).ifPresent(seat -> {
                seat.release();
                seatRepository.save(seat);
            });
        }
    }

    @Test
    @DisplayName("동시에 10명이 같은 좌석 예약 시 1명만 성공")
    void concurrentReservation_onlyOneSucceeds() throws InterruptedException {
        long concertScheduleId = System.currentTimeMillis();
        Seat seat = Seat.create(concertScheduleId, 1, 50000);
        testSeatId = seatRepository.save(seat).getId();

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final String userId = "user" + System.currentTimeMillis() + "_" + i;
            createActiveQueueToken(userId);
            final Long seatId = testSeatId;

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

        Seat result = seatRepository.findById(testSeatId).orElseThrow();
        assertThat(result.getStatus()).isEqualTo(SeatStatus.TEMPORARILY_RESERVED);
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

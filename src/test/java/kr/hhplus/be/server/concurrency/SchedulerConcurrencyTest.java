package kr.hhplus.be.server.concurrency;

import kr.hhplus.be.server.application.scheduler.SeatScheduler;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SchedulerConcurrencyTest {

    @Autowired
    private SeatScheduler seatScheduler;

    @Autowired
    private SeatRepository seatRepository;

    @Test
    @DisplayName("스케줄러 동시 실행 시 좌석 중복 해제 방지")
    void concurrentScheduler_preventsDoubleRelease() throws InterruptedException {
        List<Long> expiredSeatIds = new ArrayList<>();
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(10);
        long concertScheduleId = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) {
            Seat seat = new Seat(null, concertScheduleId, i, 50000, SeatStatus.TEMPORARILY_RESERVED,
                    "user" + i, expiredTime);
            Long seatId = seatRepository.save(seat).getId();
            expiredSeatIds.add(seatId);
        }
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        executorService.submit(() -> {
            try {
                seatScheduler.releaseExpiredReservations();
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                seatScheduler.releaseExpiredReservations();
            } finally {
                latch.countDown();
            }
        });

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        expiredSeatIds.forEach(seatId -> {
            Seat result = seatRepository.findById(seatId).orElseThrow();
            assertThat(result.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(result.getReservedBy()).isNull();
        });
    }
}

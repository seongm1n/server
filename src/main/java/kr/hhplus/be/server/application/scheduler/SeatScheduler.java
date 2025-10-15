package kr.hhplus.be.server.application.scheduler;

import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatScheduler {

    private final SeatRepository seatRepository;
    private static final int RESERVATION_TIMEOUT_MINUTES = 5;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredReservations() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(RESERVATION_TIMEOUT_MINUTES);

        List<Seat> expiredSeats = seatRepository.findExpiredWithLock(expirationTime);

        expiredSeats.forEach(seat -> {
            seat.release();
            seatRepository.save(seat);
        });

        if (!expiredSeats.isEmpty()) {
            log.info("만료된 좌석 {}개 해제 완료", expiredSeats.size());
        }
    }
}

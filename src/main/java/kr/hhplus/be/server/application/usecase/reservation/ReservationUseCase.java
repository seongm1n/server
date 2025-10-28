package kr.hhplus.be.server.application.usecase.reservation;

import kr.hhplus.be.server.application.dto.ReservationResult;
import kr.hhplus.be.server.application.usecase.seat.SeatUseCase;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.reservation.*;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.infrastructure.lock.DistributedLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class ReservationUseCase {
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final DistributedLock distributedLock;
    private final SeatUseCase seatUseCase;

    public ReservationUseCase(SeatRepository seatRepository, ReservationRepository reservationRepository, QueueTokenRepository queueTokenRepository, DistributedLock distributedLock, SeatUseCase seatUseCase) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.queueTokenRepository = queueTokenRepository;
        this.distributedLock = distributedLock;
        this.seatUseCase = seatUseCase;
    }

    @Transactional
    public ReservationResult reserve(String userId, Long seatId) {
        String lockKey = "seat:reserve:" + seatId;

        boolean lockAcquired = tryLockWithRetry(lockKey, 3, 100);
        if (!lockAcquired) {
            throw new IllegalStateException("좌석 예약 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            QueueToken queueToken = queueTokenRepository.findActiveByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("활성화된 대기열 토큰이 없습니다."));

            if (queueToken.isExpired()) {
                queueToken.expire();
                queueTokenRepository.save(queueToken);
                throw new IllegalStateException("대기열 토큰이 만료되었습니다.");
            }

            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

            seat.reserve(userId);
            seatRepository.save(seat);

            seatUseCase.evictSeatCache(seat.getConcertScheduleId());

            Reservation reservation = Reservation.create(userId, seatId, seat.getPrice());
            Reservation savedReservation = reservationRepository.save(reservation);

            return new ReservationResult(savedReservation.getId(), savedReservation.getPrice(), queueToken.getExpiresAt());
        } finally {
            distributedLock.unlock(lockKey);
        }
    }
    
    @Transactional
    public ReservationResult createReservation(String userId, Long seatId, int price) {
        Reservation reservation = Reservation.create(userId, seatId, price);
        Reservation savedReservation = reservationRepository.save(reservation);
        
        return ReservationResult.from(savedReservation);
    }
    
    public ReservationResult getReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        return ReservationResult.from(reservation);
    }

    private boolean tryLockWithRetry(String lockKey, int maxRetries, long delayMillis) {
        for (int i = 0; i < maxRetries; i++) {
            if (distributedLock.tryLock(lockKey, 5, TimeUnit.SECONDS)) {
                return true;
            }

            if (i < maxRetries - 1) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }
}

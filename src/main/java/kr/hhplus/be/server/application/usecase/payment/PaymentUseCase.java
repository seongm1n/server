package kr.hhplus.be.server.application.usecase.payment;

import kr.hhplus.be.server.application.dto.PaymentResult;
import kr.hhplus.be.server.domain.payment.*;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.reservation.*;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.domain.user.*;
import kr.hhplus.be.server.infrastructure.lock.DistributedLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class PaymentUseCase {
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserBalanceRepository userBalanceRepository;
    private final SeatRepository seatRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final DistributedLock distributedLock;

    public PaymentUseCase(PaymentRepository paymentRepository,
                         ReservationRepository reservationRepository,
                         UserBalanceRepository userBalanceRepository,
                         SeatRepository seatRepository,
                         QueueTokenRepository queueTokenRepository,
                         DistributedLock distributedLock) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.userBalanceRepository = userBalanceRepository;
        this.seatRepository = seatRepository;
        this.queueTokenRepository = queueTokenRepository;
        this.distributedLock = distributedLock;
    }

    public PaymentResult pay(String userId, Long reservationId) {
        String paymentLockKey = "payment:process:" + userId + ":" + reservationId;

        boolean lockAcquired = tryLockWithRetry(paymentLockKey, 3, 100);
        if (!lockAcquired) {
            throw new IllegalStateException("결제 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            return executePayment(userId, reservationId);
        } finally {
            distributedLock.unlock(paymentLockKey);
        }
    }

    @Transactional
    private PaymentResult executePayment(String userId, Long reservationId) {
        QueueToken queueToken = queueTokenRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 대기열 토큰이 없습니다."));

        if (queueToken.isExpired()) {
            queueToken.expire();
            queueTokenRepository.save(queueToken);
            throw new IllegalStateException("대기열 토큰이 만료되었습니다.");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("예약자와 결제자가 일치하지 않습니다.");
        }

        String balanceLockKey = "balance:use:" + userId;
        boolean balanceLockAcquired = tryLockWithRetry(balanceLockKey, 3, 100);
        if (!balanceLockAcquired) {
            throw new IllegalStateException("잔액 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            UserBalance userBalance = userBalanceRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 잔액 정보를 찾을 수 없습니다."));

            userBalance.use(reservation.getPrice());
            userBalanceRepository.save(userBalance);
        } finally {
            distributedLock.unlock(balanceLockKey);
        }

        reservation.confirm();
        reservationRepository.save(reservation);

        Seat seat = seatRepository.findById(reservation.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));
        seat.confirm();
        seatRepository.save(seat);

        Payment payment = Payment.create(userId, reservationId, reservation.getPrice());
        payment.complete();
        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentResult(savedPayment.getId(), savedPayment.getStatus().name());
    }
    
    public PaymentResult processPayment(String userId, Long reservationId) {
        return pay(userId, reservationId);
    }

    private boolean tryLockWithRetry(String lockKey, int maxRetries, long delayMillis) {
        for (int i = 0; i < maxRetries; i++) {
            if (distributedLock.tryLock(lockKey, 10, TimeUnit.SECONDS)) {
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

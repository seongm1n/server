package kr.hhplus.be.server.application.usecase.payment;

import kr.hhplus.be.server.application.dto.PaymentResult;
import kr.hhplus.be.server.domain.payment.*;
import kr.hhplus.be.server.domain.reservation.*;
import kr.hhplus.be.server.domain.user.*;
import org.springframework.stereotype.Service;

@Service
public class PaymentUseCase {
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserBalanceRepository userBalanceRepository;
    private final SeatRepository seatRepository;

    public PaymentUseCase(PaymentRepository paymentRepository, 
                         ReservationRepository reservationRepository,
                         UserBalanceRepository userBalanceRepository,
                         SeatRepository seatRepository) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.userBalanceRepository = userBalanceRepository;
        this.seatRepository = seatRepository;
    }

    public PaymentResult pay(String userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        UserBalance userBalance = userBalanceRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 잔액 정보를 찾을 수 없습니다."));

        Seat seat = seatRepository.findById(reservation.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        userBalance.use(reservation.getPrice());
        userBalanceRepository.save(userBalance);

        reservation.confirm();
        reservationRepository.save(reservation);

        seat.confirm();
        seatRepository.save(seat);

        Payment payment = Payment.create(userId, reservationId, reservation.getPrice());
        payment.complete();
        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentResult(savedPayment.getId(), savedPayment.getStatus().name());
    }
}

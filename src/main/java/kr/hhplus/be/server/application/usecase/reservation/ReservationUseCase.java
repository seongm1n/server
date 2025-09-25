package kr.hhplus.be.server.application.usecase.reservation;

import kr.hhplus.be.server.application.dto.ReservationResult;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.reservation.*;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import org.springframework.stereotype.Service;

@Service
public class ReservationUseCase {
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final QueueTokenRepository queueTokenRepository;

    public ReservationUseCase(SeatRepository seatRepository, ReservationRepository reservationRepository, QueueTokenRepository queueTokenRepository) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.queueTokenRepository = queueTokenRepository;
    }

    public ReservationResult reserve(String userId, Long seatId) {
        QueueToken queueToken = queueTokenRepository.findActiveByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 대기열 토큰이 없습니다."));

        if (queueToken.isExpired()) {
            queueToken.expire();
            queueTokenRepository.save(queueToken);
            throw new IllegalStateException("대기열 토큰이 만료되었습니다.");
        }

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        if (seat.getStatus() == SeatStatus.TEMPORARILY_RESERVED && seat.getReservedBy() != null) {
            QueueToken reserverToken = queueTokenRepository.findActiveByUserId(seat.getReservedBy()).orElse(null);
            if (reserverToken == null || reserverToken.isExpired()) {
                seat.release();
                if (reserverToken != null && reserverToken.isExpired()) {
                    reserverToken.expire();
                    queueTokenRepository.save(reserverToken);
                }
            }
        }

        seat.reserve(userId);
        seatRepository.save(seat);

        Reservation reservation = Reservation.create(userId, seatId, seat.getPrice());
        Reservation savedReservation = reservationRepository.save(reservation);

        return new ReservationResult(savedReservation.getId(), savedReservation.getPrice(), queueToken.getExpiresAt());
    }
}

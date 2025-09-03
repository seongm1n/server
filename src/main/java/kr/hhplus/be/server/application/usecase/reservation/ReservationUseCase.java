package kr.hhplus.be.server.application.usecase.reservation;

import kr.hhplus.be.server.application.dto.ReservationResult;
import kr.hhplus.be.server.domain.reservation.*;
import org.springframework.stereotype.Service;

@Service
public class ReservationUseCase {
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    public ReservationUseCase(SeatRepository seatRepository, ReservationRepository reservationRepository) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationResult reserve(String userId, Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        seat.reserve(userId);
        seatRepository.save(seat);

        Reservation reservation = Reservation.create(userId, seatId, seat.getPrice());
        Reservation savedReservation = reservationRepository.save(reservation);

        return new ReservationResult(savedReservation.getId(), savedReservation.getPrice(), savedReservation.getExpiresAt());
    }
}

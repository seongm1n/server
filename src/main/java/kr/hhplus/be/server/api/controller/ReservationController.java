package kr.hhplus.be.server.api.controller;

import kr.hhplus.be.server.api.dto.ReservationRequest;
import kr.hhplus.be.server.application.dto.ReservationResult;
import kr.hhplus.be.server.application.usecase.reservation.ReservationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationUseCase reservationUseCase;

    @PostMapping
    public ResponseEntity<ReservationResult> reserve(@RequestBody ReservationRequest request) {
        ReservationResult result = reservationUseCase.reserve(request.userId(), request.seatId());
        return ResponseEntity.ok(result);
    }
}

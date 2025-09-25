package kr.hhplus.be.server.api.controller;

import kr.hhplus.be.server.api.dto.SeatReserveRequest;
import kr.hhplus.be.server.application.dto.SeatResult;
import kr.hhplus.be.server.application.usecase.seat.SeatUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatUseCase seatUseCase;

    @GetMapping("/{seatId}")
    public ResponseEntity<SeatResult> getSeat(@PathVariable Long seatId) {
        SeatResult result = seatUseCase.getSeat(seatId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<SeatResult>> getAvailableSeats(@PathVariable Long scheduleId) {
        List<SeatResult> results = seatUseCase.getAvailableSeats(scheduleId);
        return ResponseEntity.ok(results);
    }

    @PatchMapping("/{seatId}/reserve")
    public ResponseEntity<SeatResult> reserveSeat(
            @PathVariable Long seatId, 
            @RequestBody SeatReserveRequest request) {
        SeatResult result = seatUseCase.reserveSeat(seatId, request.userId());
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{seatId}/confirm")
    public ResponseEntity<SeatResult> confirmSeat(@PathVariable Long seatId) {
        SeatResult result = seatUseCase.confirmSeat(seatId);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{seatId}/release")
    public ResponseEntity<SeatResult> releaseSeat(@PathVariable Long seatId) {
        SeatResult result = seatUseCase.releaseSeat(seatId);
        return ResponseEntity.ok(result);
    }
}

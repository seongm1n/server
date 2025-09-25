package kr.hhplus.be.server.api.controller;

import kr.hhplus.be.server.application.usecase.concert.ConcertUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertUseCase concertUseCase;

    @GetMapping("/{concertId}/dates")
    public ResponseEntity<List<LocalDate>> getAvailableDates(@PathVariable Long concertId) {
        List<LocalDate> dates = concertUseCase.getAvailableDates(concertId);
        return ResponseEntity.ok(dates);
    }

    @GetMapping("/schedules/{scheduleId}/seats")
    public ResponseEntity<List<Integer>> getAvailableSeats(@PathVariable Long scheduleId) {
        List<Integer> seatNumbers = concertUseCase.getAvailableSeats(scheduleId);
        return ResponseEntity.ok(seatNumbers);
    }
}

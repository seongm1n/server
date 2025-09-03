package kr.hhplus.be.server.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ReservationResult {
    private Long reservationId;
    private int price;
    private LocalDateTime expiresAt;
}

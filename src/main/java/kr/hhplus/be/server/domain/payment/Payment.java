package kr.hhplus.be.server.domain.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Payment {
    private Long id;
    private String userId;
    private Long reservationId;
    private int amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String failureReason;

    public static Payment create(String userId, Long reservationId, int amount) {
        return new Payment(null, userId, reservationId, amount, PaymentStatus.PENDING, 
                          LocalDateTime.now(), null, null);
    }

    public void complete() {
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}

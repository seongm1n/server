package kr.hhplus.be.server.infrastructure.persistence.payment;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;
    
    @Column(name = "amount", nullable = false)
    private int amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    public static PaymentEntity from(Payment payment) {
        return new PaymentEntity(
                payment.getId(),
                payment.getUserId(),
                payment.getReservationId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getCompletedAt(),
                payment.getFailureReason()
        );
    }
    
    public Payment toDomain() {
        return new Payment(
                this.id,
                this.userId,
                this.reservationId,
                this.amount,
                this.status,
                this.createdAt,
                this.completedAt,
                this.failureReason
        );
    }
    
    public void updateStatus(PaymentStatus status, LocalDateTime completedAt, String failureReason) {
        this.status = status;
        this.completedAt = completedAt;
        this.failureReason = failureReason;
    }
}

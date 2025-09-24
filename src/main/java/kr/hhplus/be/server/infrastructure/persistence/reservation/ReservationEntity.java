package kr.hhplus.be.server.infrastructure.persistence.reservation;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "seat_id", nullable = false)
    private Long seatId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;
    
    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    public static ReservationEntity from(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now();
        return new ReservationEntity(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getSeatId(),
                reservation.getStatus(),
                reservation.getCreatedAt() != null ? reservation.getCreatedAt() : now,
                now.plusMinutes(10) // 10분 후 만료
        );
    }
    
    public Reservation toDomain() {
        return new Reservation(
                this.id,
                this.userId,
                this.seatId,
                0, // price는 seat에서 조회 필요
                this.status,
                this.reservedAt
        );
    }
    
    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }
}

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
    
    @Column(name = "price", nullable = false)
    private int price;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public static ReservationEntity from(Reservation reservation) {
        return new ReservationEntity(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getSeatId(),
                reservation.getStatus(),
                reservation.getPrice(),
                reservation.getCreatedAt()
        );
    }
    
    public Reservation toDomain() {
        return new Reservation(
                this.id,
                this.userId,
                this.seatId,
                this.price,
                this.status,
                this.createdAt
        );
    }
    
    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }
}

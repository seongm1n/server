package kr.hhplus.be.server.infrastructure.persistence.seat;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"concert_schedule_id", "seat_number"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SeatEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "concert_schedule_id", nullable = false)
    private Long concertScheduleId;
    
    @Column(name = "seat_number", nullable = false)
    private int seatNumber;
    
    @Column(name = "price", nullable = false)
    private int price;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeatStatus status;
    
    @Column(name = "reserved_by")
    private String reservedBy;
    
    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;
    
    public static SeatEntity from(Seat seat) {
        return new SeatEntity(
                seat.getId(),
                seat.getConcertScheduleId(),
                seat.getSeatNumber(),
                seat.getPrice(),
                seat.getStatus(),
                seat.getReservedBy(),
                seat.getReservedAt()
        );
    }
    
    public Seat toDomain() {
        return new Seat(
                this.id,
                this.concertScheduleId,
                this.seatNumber,
                this.price,
                this.status,
                this.reservedBy,
                this.reservedAt
        );
    }
    
    public void updateReservation(SeatStatus status, String reservedBy, LocalDateTime reservedAt) {
        this.status = status;
        this.reservedBy = reservedBy;
        this.reservedAt = reservedAt;
    }
}

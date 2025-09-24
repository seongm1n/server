package kr.hhplus.be.server.infrastructure.persistence.concert;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "concert_schedule")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConcertScheduleEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "concert_id", nullable = false)
    private Long concertId;
    
    @Column(name = "concert_date", nullable = false)
    private LocalDate concertDate;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "total_seats", nullable = false)
    private int totalSeats;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

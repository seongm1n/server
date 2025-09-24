package kr.hhplus.be.server.infrastructure.persistence.queue;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "queue_token")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QueueTokenEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(name = "position_num", nullable = false)
    private int position;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private kr.hhplus.be.server.domain.queue.QueueStatus status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    public static QueueTokenEntity from(kr.hhplus.be.server.domain.queue.QueueToken token) {
        return new QueueTokenEntity(
            token.getId(),
            token.getUserId(),
            token.getToken(),
            token.getPosition(),
            token.getStatus(),
            token.getCreatedAt(),
            token.getActivatedAt(),
            token.getExpiresAt()
        );
    }
    
    public kr.hhplus.be.server.domain.queue.QueueToken toDomain() {
        return new kr.hhplus.be.server.domain.queue.QueueToken(
            this.id,
            this.userId,
            this.token,
            this.position,
            this.status,
            this.createdAt,
            this.activatedAt,
            this.expiresAt
        );
    }
    
    public void updateStatus(kr.hhplus.be.server.domain.queue.QueueStatus status, 
                           LocalDateTime activatedAt, LocalDateTime expiresAt) {
        this.status = status;
        this.activatedAt = activatedAt;
        this.expiresAt = expiresAt;
    }
}
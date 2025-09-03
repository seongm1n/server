package kr.hhplus.be.server.domain.queue;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class QueueToken {
    private Long id;
    private String userId;
    private String token;
    private int position;
    private QueueStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime activatedAt;
    private LocalDateTime expiresAt;

    public static QueueToken create(String userId, int position) {
        String token = UUID.randomUUID().toString();
        return new QueueToken(null, userId, token, position, QueueStatus.WAITING,
                            LocalDateTime.now(), null, null);
    }

    public void activate() {
        this.status = QueueStatus.ACTIVE;
        this.activatedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(10);
    }


    public boolean isExpired() {
        if (status != QueueStatus.ACTIVE || expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

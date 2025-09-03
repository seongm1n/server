package kr.hhplus.be.server.domain.queue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("대기열 토큰 도메인 테스트")
class QueueTokenTest {

    @Test
    @DisplayName("대기열 토큰을 생성할 수 있다")
    void createQueueToken() {
        String userId = "user123";
        int position = 10;

        QueueToken token = QueueToken.create(userId, position);

        assertThat(token.getUserId()).isEqualTo(userId);
        assertThat(token.getPosition()).isEqualTo(position);
        assertThat(token.getStatus()).isEqualTo(QueueStatus.WAITING);
        assertThat(token.getToken()).isNotNull();
        assertThat(token.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("토큰을 활성화할 수 있다")
    void activateToken() {
        QueueToken token = QueueToken.create("user123", 1);

        token.activate();

        assertThat(token.getStatus()).isEqualTo(QueueStatus.ACTIVE);
        assertThat(token.getActivatedAt()).isNotNull();
        assertThat(token.getExpiresAt()).isAfter(LocalDateTime.now());
    }


    @Test
    @DisplayName("토큰이 만료되었는지 확인할 수 있다")
    void isExpired() {
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(1);
        QueueToken token = new QueueToken(1L, "user123", "token123", 1, QueueStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), pastTime);

        assertThat(token.isExpired()).isTrue();
    }

    @Test
    @DisplayName("활성 상태가 아닌 토큰은 만료되지 않는다")
    void waitingTokenNeverExpires() {
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(1);
        QueueToken token = new QueueToken(1L, "user123", "token123", 1, QueueStatus.WAITING,
                LocalDateTime.now(), null, pastTime);

        assertThat(token.isExpired()).isFalse();
    }

}

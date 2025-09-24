package kr.hhplus.be.server.infrastructure.persistence.queue;

import kr.hhplus.be.server.domain.queue.QueueStatus;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueueTokenRepositoryImpl.class})
@ActiveProfiles("test")
class QueueTokenRepositoryImplTest {

    @Autowired
    private QueueTokenRepository repository;

    @Test
    void 대기열_토큰_저장_조회() {
        QueueToken token = QueueToken.create("user1", 1);

        QueueToken saved = repository.save(token);
        Optional<QueueToken> found = repository.findByToken(saved.getToken());

        assertThat(saved.getId()).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo("user1");
        assertThat(found.get().getPosition()).isEqualTo(1);
        assertThat(found.get().getStatus()).isEqualTo(QueueStatus.WAITING);
    }

    @Test
    void 존재하지_않는_토큰_조회() {
        Optional<QueueToken> found = repository.findByToken("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void 사용자_활성_토큰_조회() {
        QueueToken waitingToken = QueueToken.create("user1", 1);
        QueueToken activeToken = QueueToken.create("user2", 2);
        activeToken.activate();

        repository.save(waitingToken);
        repository.save(activeToken);

        Optional<QueueToken> found = repository.findActiveByUserId("user2");
        Optional<QueueToken> notFound = repository.findActiveByUserId("user1");

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(QueueStatus.ACTIVE);
        assertThat(notFound).isEmpty();
    }

    @Test
    void 대기_토큰_개수_조회() {
        QueueToken waiting1 = QueueToken.create("user1", 1);
        QueueToken waiting2 = QueueToken.create("user2", 2);
        QueueToken active = QueueToken.create("user3", 3);
        active.activate();

        repository.save(waiting1);
        repository.save(waiting2);
        repository.save(active);

        Long waitingCount = repository.countWaitingTokens();
        Long activeCount = repository.countActiveTokens();

        assertThat(waitingCount).isEqualTo(2);
        assertThat(activeCount).isEqualTo(1);
    }

    @Test
    void 활성화할_대기_토큰_조회() {
        QueueToken token1 = QueueToken.create("user1", 1);
        QueueToken token2 = QueueToken.create("user2", 2);
        QueueToken token3 = QueueToken.create("user3", 3);

        repository.save(token1);
        repository.save(token2);
        repository.save(token3);

        List<QueueToken> tokensToActivate = repository.getWaitingTokensToActivate(2);

        assertThat(tokensToActivate).hasSize(2);
        assertThat(tokensToActivate.get(0).getPosition()).isEqualTo(1);
        assertThat(tokensToActivate.get(1).getPosition()).isEqualTo(2);
    }

    @Test
    void 토큰_상태_변경_저장() {
        QueueToken token = QueueToken.create("user1", 1);
        repository.save(token);

        QueueToken found = repository.findByToken(token.getToken()).get();
        found.activate();
        repository.save(found);

        QueueToken updated = repository.findByToken(token.getToken()).get();
        assertThat(updated.getStatus()).isEqualTo(QueueStatus.ACTIVE);
        assertThat(updated.getActivatedAt()).isNotNull();
        assertThat(updated.getExpiresAt()).isNotNull();
    }
}
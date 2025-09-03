package kr.hhplus.be.server.application;

import kr.hhplus.be.server.application.dto.QueueTokenResult;
import kr.hhplus.be.server.application.usecase.queue.QueueUseCase;
import kr.hhplus.be.server.domain.queue.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("대기열 UseCase 테스트")
class QueueUseCaseTest {

    @Mock
    private QueueTokenRepository queueTokenRepository;

    private QueueUseCase queueUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        queueUseCase = new QueueUseCase(queueTokenRepository);
    }

    @Test
    @DisplayName("대기열 토큰을 발급할 수 있다")
    void issueToken() {
        String userId = "user123";
        
        when(queueTokenRepository.countWaitingTokens()).thenReturn(10L);
        when(queueTokenRepository.save(any(QueueToken.class)))
            .thenAnswer(invocation -> {
                QueueToken token = invocation.getArgument(0);
                return new QueueToken(1L, token.getUserId(), token.getToken(), 
                                    token.getPosition(), token.getStatus(), 
                                    token.getCreatedAt(), token.getActivatedAt(), token.getExpiresAt());
            });

        QueueTokenResult result = queueUseCase.issueToken(userId);

        assertThat(result.getToken()).isNotNull();
        assertThat(result.getQueuePosition()).isEqualTo(11);
        assertThat(result.getStatus()).isEqualTo("WAITING");
        
        verify(queueTokenRepository).save(any(QueueToken.class));
    }

    @Test
    @DisplayName("대기열 상태를 조회할 수 있다")
    void getQueueStatus() {
        String token = "token123";
        QueueToken queueToken = new QueueToken(1L, "user123", token, 5, 
                                              QueueStatus.WAITING, null, null, null);
        
        when(queueTokenRepository.findByToken(token)).thenReturn(Optional.of(queueToken));

        QueueTokenResult result = queueUseCase.getQueueStatus(token);

        assertThat(result.getQueuePosition()).isEqualTo(5);
        assertThat(result.getStatus()).isEqualTo("WAITING");
    }

    @Test
    @DisplayName("존재하지 않는 토큰은 조회할 수 없다")
    void cannotGetStatusForNonExistentToken() {
        String token = "invalid-token";
        
        when(queueTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queueUseCase.getQueueStatus(token))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("유효하지 않은 토큰입니다.");
    }

    @Test
    @DisplayName("대기열에서 활성화할 토큰들을 처리할 수 있다")
    void activateWaitingTokens() {
        int maxActiveTokens = 100;
        
        when(queueTokenRepository.countActiveTokens()).thenReturn(80L);
        when(queueTokenRepository.getWaitingTokensToActivate(20)).thenReturn(java.util.Arrays.asList(
            QueueToken.create("user1", 1),
            QueueToken.create("user2", 2)
        ));

        queueUseCase.activateWaitingTokens(maxActiveTokens);

        verify(queueTokenRepository).saveAll(any());
    }
}

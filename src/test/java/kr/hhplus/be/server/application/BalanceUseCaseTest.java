package kr.hhplus.be.server.application;

import kr.hhplus.be.server.application.dto.BalanceResult;
import kr.hhplus.be.server.application.usecase.balance.BalanceUseCase;
import kr.hhplus.be.server.domain.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("잔액 UseCase 테스트")
class BalanceUseCaseTest {

    @Mock
    private UserBalanceRepository userBalanceRepository;

    private BalanceUseCase balanceUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        balanceUseCase = new BalanceUseCase(userBalanceRepository);
    }

    @Test
    @DisplayName("잔액을 충전할 수 있다")
    void chargeBalance() {
        String userId = "user123";
        int chargeAmount = 50000;
        
        UserBalance userBalance = UserBalance.create(userId, 100000);
        
        when(userBalanceRepository.findByUserId(userId)).thenReturn(Optional.of(userBalance));
        when(userBalanceRepository.save(any(UserBalance.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        BalanceResult result = balanceUseCase.charge(userId, chargeAmount);

        assertThat(result.getBalance()).isEqualTo(150000);
        verify(userBalanceRepository).save(userBalance);
    }

    @Test
    @DisplayName("새 사용자의 경우 잔액을 생성하여 충전할 수 있다")
    void chargeBalanceForNewUser() {
        String userId = "newuser";
        int chargeAmount = 30000;
        
        when(userBalanceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userBalanceRepository.save(any(UserBalance.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        BalanceResult result = balanceUseCase.charge(userId, chargeAmount);

        assertThat(result.getBalance()).isEqualTo(30000);
        verify(userBalanceRepository).save(any(UserBalance.class));
    }

    @Test
    @DisplayName("잔액을 조회할 수 있다")
    void getBalance() {
        String userId = "user123";
        UserBalance userBalance = UserBalance.create(userId, 75000);
        
        when(userBalanceRepository.findByUserId(userId)).thenReturn(Optional.of(userBalance));

        BalanceResult result = balanceUseCase.getBalance(userId);

        assertThat(result.getBalance()).isEqualTo(75000);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 잔액은 0이다")
    void getBalanceForNonExistentUser() {
        String userId = "nonexistent";
        
        when(userBalanceRepository.findByUserId(userId)).thenReturn(Optional.empty());

        BalanceResult result = balanceUseCase.getBalance(userId);

        assertThat(result.getBalance()).isEqualTo(0);
    }
}

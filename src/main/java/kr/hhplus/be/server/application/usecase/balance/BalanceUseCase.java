package kr.hhplus.be.server.application.usecase.balance;

import kr.hhplus.be.server.application.dto.BalanceResult;
import kr.hhplus.be.server.domain.user.*;
import org.springframework.stereotype.Service;

@Service
public class BalanceUseCase {
    private final UserBalanceRepository userBalanceRepository;

    public BalanceUseCase(UserBalanceRepository userBalanceRepository) {
        this.userBalanceRepository = userBalanceRepository;
    }

    public BalanceResult charge(String userId, int amount) {
        UserBalance userBalance = userBalanceRepository.findByUserId(userId)
                .orElse(UserBalance.create(userId, 0));
        
        userBalance.charge(amount);
        userBalanceRepository.save(userBalance);
        
        return new BalanceResult(userBalance.getBalance());
    }

    public BalanceResult getBalance(String userId) {
        UserBalance userBalance = userBalanceRepository.findByUserId(userId)
                .orElse(UserBalance.create(userId, 0));
        
        return new BalanceResult(userBalance.getBalance());
    }
}

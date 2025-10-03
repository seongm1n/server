package kr.hhplus.be.server.infrastructure.persistence.user;

import kr.hhplus.be.server.domain.user.UserBalance;
import kr.hhplus.be.server.domain.user.UserBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserBalanceRepositoryImpl implements UserBalanceRepository {
    
    private final UserBalanceJpaRepository jpaRepository;
    
    @Override
    public Optional<UserBalance> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId)
                .map(UserBalanceEntity::toDomain);
    }
    
    @Override
    public UserBalance save(UserBalance userBalance) {
        Optional<UserBalanceEntity> existingEntity = jpaRepository.findByUserId(userBalance.getUserId());
        
        if (existingEntity.isPresent()) {
            UserBalanceEntity entity = existingEntity.get();
            entity.updateBalance(userBalance.getBalance(), userBalance.getUpdatedAt());
            return jpaRepository.save(entity).toDomain();
        } else {
            UserBalanceEntity entity = UserBalanceEntity.from(userBalance);
            return jpaRepository.save(entity).toDomain();
        }
    }
}

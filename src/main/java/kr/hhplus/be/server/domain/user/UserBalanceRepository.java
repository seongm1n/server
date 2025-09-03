package kr.hhplus.be.server.domain.user;

import java.util.Optional;

public interface UserBalanceRepository {
    Optional<UserBalance> findByUserId(String userId);
    UserBalance save(UserBalance userBalance);
}

package kr.hhplus.be.server.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBalanceJpaRepository extends JpaRepository<UserBalanceEntity, Long> {
    Optional<UserBalanceEntity> findByUserId(String userId);
}

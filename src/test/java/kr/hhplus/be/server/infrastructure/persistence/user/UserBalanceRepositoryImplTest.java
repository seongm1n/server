package kr.hhplus.be.server.infrastructure.persistence.user;

import kr.hhplus.be.server.domain.user.UserBalance;
import kr.hhplus.be.server.infrastructure.persistence.user.UserBalanceRepositoryImpl;
import kr.hhplus.be.server.infrastructure.persistence.user.UserBalanceJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserBalanceRepositoryImplTest extends kr.hhplus.be.server.infrastructure.persistence.TestContainerConfig {

    @Autowired
    private UserBalanceJpaRepository jpaRepository;
    
    private UserBalanceRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new UserBalanceRepositoryImpl(jpaRepository);
    }

    @Test
    void 사용자_잔액_저장_조회() {
        UserBalance userBalance = UserBalance.create("user1", 10000);

        UserBalance saved = repository.save(userBalance);
        Optional<UserBalance> found = repository.findByUserId("user1");

        assertThat(saved.getId()).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo("user1");
        assertThat(found.get().getBalance()).isEqualTo(10000);
    }

    @Test
    void 존재하지_않는_사용자_조회() {
        Optional<UserBalance> found = repository.findByUserId("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void 잔액_충전_테스트() {
        UserBalance userBalance = UserBalance.create("user1", 10000);
        repository.save(userBalance);

        UserBalance found = repository.findByUserId("user1").get();
        found.charge(5000);
        repository.save(found);

        UserBalance result = repository.findByUserId("user1").get();
        assertThat(result.getBalance()).isEqualTo(15000);
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void 잔액_사용_테스트() {
        UserBalance userBalance = UserBalance.create("user1", 10000);
        repository.save(userBalance);

        UserBalance found = repository.findByUserId("user1").get();
        found.use(3000);
        repository.save(found);

        UserBalance result = repository.findByUserId("user1").get();
        assertThat(result.getBalance()).isEqualTo(7000);
    }
}

package kr.hhplus.be.server.concurrency;

import kr.hhplus.be.server.application.usecase.balance.BalanceUseCase;
import kr.hhplus.be.server.domain.user.UserBalance;
import kr.hhplus.be.server.domain.user.UserBalanceRepository;
import kr.hhplus.be.server.infrastructure.persistence.TestContainerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BalanceConcurrencyTest extends TestContainerConfig {

    @Autowired
    private BalanceUseCase balanceUseCase;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Test
    @DisplayName("동시에 5번 3000원 차감 시 3번만 성공하고 잔액 음수 방지")
    void concurrentDeduction_preventsNegativeBalance() throws InterruptedException {
        String testUserId = "testUser_" + System.currentTimeMillis();
        UserBalance balance = UserBalance.create(testUserId, 10000);
        userBalanceRepository.save(balance);
        int threadCount = 5;
        int deductAmount = 3000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    balanceUseCase.use(testUserId, deductAmount);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 잔액 부족 또는 낙관적 락 실패
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(successCount.get()).isEqualTo(3);

        UserBalance result = userBalanceRepository.findByUserId(testUserId).orElseThrow();
        assertThat(result.getBalance()).isEqualTo(1000);
        assertThat(result.getBalance()).isGreaterThanOrEqualTo(0);
    }
}

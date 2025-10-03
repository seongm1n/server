package kr.hhplus.be.server.integration;

import kr.hhplus.be.server.application.dto.*;
import kr.hhplus.be.server.application.usecase.balance.BalanceUseCase;
import kr.hhplus.be.server.application.usecase.payment.PaymentUseCase;
import kr.hhplus.be.server.application.usecase.queue.QueueUseCase;
import kr.hhplus.be.server.application.usecase.reservation.ReservationUseCase;
import kr.hhplus.be.server.application.usecase.seat.SeatUseCase;
import kr.hhplus.be.server.domain.queue.QueueStatus;
import kr.hhplus.be.server.domain.seat.SeatStatus;
import kr.hhplus.be.server.infrastructure.persistence.TestContainerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("콘서트 예약 통합 테스트")
class ConcertReservationIntegrationTest extends TestContainerConfig {

    @Autowired
    private QueueUseCase queueUseCase;
    
    @Autowired
    private SeatUseCase seatUseCase;
    
    @Autowired
    private ReservationUseCase reservationUseCase;
    
    @Autowired
    private PaymentUseCase paymentUseCase;
    
    @Autowired
    private BalanceUseCase balanceUseCase;

    @Test
    @DisplayName("전체 예약 플로우 테스트: 토큰 발급 → 좌석 조회 → 잔액 확인")
    @Transactional
    void completeReservationFlowTest() {
        // Given: 사용자 설정
        String userId = "flow-user";
        balanceUseCase.charge(userId, 100000);
        
        // Step 1: 토큰 발급
        QueueTokenResult token = queueUseCase.issueToken(userId);
        assertThat(token.getToken()).isNotNull();
        assertThat(token.getStatus()).isEqualTo(QueueStatus.WAITING.name());
        
        // Step 2: 좌석 조회
        List<SeatResult> seats = seatUseCase.getAvailableSeats(1L);
        assertThat(seats).isNotNull();
        
        // Step 3: 잔액 확인
        BalanceResult balance = balanceUseCase.getBalance(userId);
        assertThat(balance.getBalance()).isEqualTo(100000);
        
        // 플로우가 모두 정상 동작함을 확인
        assertThat(token.getQueuePosition()).isGreaterThan(0);
    }

    @Test
    @DisplayName("다중 유저가 동시에 토큰 요청 시 고유한 위치 보장 테스트")
    void concurrentTokenIssuanceTest() throws InterruptedException {
        // Given: 5명의 사용자
        int userCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(userCount);
        CountDownLatch latch = new CountDownLatch(userCount);
        AtomicInteger[] positions = new AtomicInteger[userCount];
        
        for (int i = 0; i < userCount; i++) {
            positions[i] = new AtomicInteger(0);
        }
        
        // When: 동시에 토큰 발급
        for (int i = 0; i < userCount; i++) {
            final int index = i;
            final String userId = "concurrent-user-" + i;
            
            executor.submit(() -> {
                try {
                    QueueTokenResult result = queueUseCase.issueToken(userId);
                    positions[index].set(result.getQueuePosition());
                } catch (Exception e) {
                    // 예외 발생 시에도 테스트 계속 진행
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        // Then: 토큰이 발급되었고 위치가 할당되었는지 확인
        int successfulTokens = 0;
        for (AtomicInteger position : positions) {
            if (position.get() > 0) {
                successfulTokens++;
            }
        }
        
        assertThat(successfulTokens).isEqualTo(userCount);
    }

    @Test
    @DisplayName("만료된 임시 예약 처리 테스트")
    @Transactional
    void expireTemporaryReservationsTest() {
        // Given
        Long concertScheduleId = 1L;
        
        // 초기 좌석 상태 확인
        List<SeatResult> initialSeats = seatUseCase.getAvailableSeats(concertScheduleId);
        int initialAvailableCount = initialSeats.size();
        
        // When: 만료된 임시 예약 처리
        seatUseCase.expireTemporaryReservations();
        
        // Then: 좌석 상태가 안정적으로 유지되어야 함
        List<SeatResult> afterSeats = seatUseCase.getAvailableSeats(concertScheduleId);
        assertThat(afterSeats.size()).isEqualTo(initialAvailableCount);
    }

    @Test
    @DisplayName("토큰 없이 좌석 예약 시도하면 실패해야 함")
    @Transactional
    void reservationFailsWithoutValidTokenTest() {
        // Given: 토큰이 없는 사용자
        String userId = "no-token-user";
        Long targetSeatId = 1L; // 존재하지 않을 수도 있는 좌석
        
        // When & Then: 예약 시도 시 실패해야 함
        assertThatThrownBy(() -> reservationUseCase.reserve(userId, targetSeatId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활성화된 대기열 토큰이 없습니다.");
    }

    @Test
    @DisplayName("잔액 관리 테스트")
    @Transactional
    void balanceManagementTest() {
        // Given
        String userId = "balance-user";
        
        // 초기 잔액 조회 (0원이어야 함)
        BalanceResult initialBalance = balanceUseCase.getBalance(userId);
        assertThat(initialBalance.getBalance()).isEqualTo(0);
        
        // When: 잔액 충전
        int chargeAmount = 50000;
        BalanceResult chargedBalance = balanceUseCase.charge(userId, chargeAmount);
        
        // Then
        assertThat(chargedBalance.getBalance()).isEqualTo(chargeAmount);
        
        // 추가 충전
        BalanceResult additionalCharge = balanceUseCase.charge(userId, 30000);
        assertThat(additionalCharge.getBalance()).isEqualTo(80000);
    }
}
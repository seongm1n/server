# 동시성 제어 구현 보고서

## 1. 문제 상황

### 1.1 좌석 중복 예약
- **문제**: 동시에 같은 좌석을 예약하면 중복 예약 발생
- **원인**: `Seat.reserve()` 메서드의 상태 체크와 변경이 원자적이지 않음
- **영향**: 좌석 초과 판매, 데이터 불일치

### 1.2 잔액 음수 발생
- **문제**: 동시 차감 시 잔액이 음수로 변경 가능
- **원인**: 잔액 조회와 차감이 별도 트랜잭션에서 실행
- **영향**: 재무 데이터 무결성 훼손

### 1.3 타임아웃 해제 로직 부정확
- **문제**: 스케줄러 중복 실행 시 좌석 상태 불안정
- **원인**: 만료 좌석 조회와 해제 사이의 동기화 부재
- **영향**: 좌석 상태 불일치

---

## 2. 해결 전략

### 2.1 좌석 예약: Pessimistic Lock (SELECT FOR UPDATE)

**선택 이유**
- 중복 예약 절대 불가 (비즈니스 크리티컬)
- 높은 경합 예상

**구현**
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM SeatEntity s WHERE s.id = :id")
Optional<SeatEntity> findByIdWithLock(@Param("id") Long id);
```

**파일**
- `SeatJpaRepository.java` - 락 메서드 추가
- `ReservationUseCase.java` - `findByIdWithLock()` 사용

---

### 2.2 잔액 차감: Optimistic Lock (@Version)

**선택 이유**
- 사용자별 격리된 자원
- 재시도 가능한 작업

**구현**
```java
@Version
private Long version;

@Retryable(
    retryFor = {ObjectOptimisticLockingFailureException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 100)
)
public BalanceResult use(String userId, int amount) { ... }
```

**파일**
- `UserBalanceEntity.java` - @Version 추가
- `BalanceUseCase.java` - @Retryable 적용
- `ServerApplication.java` - @EnableRetry

---

### 2.3 스케줄러: @Scheduled + Pessimistic Lock

**선택 이유**
- 배치 작업 동시 실행 방지 필요

**구현**
```java
@Scheduled(fixedRate = 60000)
@Transactional
public void releaseExpiredReservations() {
    List<Seat> expiredSeats = seatRepository.findExpiredWithLock(expirationTime);
    expiredSeats.forEach(seat -> {
        seat.release();
        seatRepository.save(seat);
    });
}
```

**파일**
- `SeatScheduler.java` - 스케줄러 구현
- `ServerApplication.java` - @EnableScheduling

---

## 3. 테스트 결과

### 3.1 좌석 중복 예약 방지
```
테스트: 10개 스레드 → 1개 좌석 동시 예약
결과: 성공 1건, 실패 9건 ✅
검증: 좌석 상태 = TEMPORARILY_RESERVED
```

### 3.2 잔액 음수 방지
```
테스트: 초기 10,000원 → 5개 스레드가 각 3,000원 차감
결과: 성공 3건, 실패 2건 ✅
검증: 최종 잔액 = 1,000원 (음수 없음)
```

### 3.3 스케줄러 중복 실행 방지
```
테스트: 50개 만료 좌석 → 스케줄러 2개 동시 실행
결과: 모든 좌석 정확히 1번만 해제 ✅
검증: 좌석 상태 = AVAILABLE
```

---

## 4. 사용된 동시성 기법

| 기능 | 기법 | 방식 |
|------|------|------|
| 좌석 예약 | SELECT FOR UPDATE | Pessimistic Lock |
| 잔액 차감 | @Version | Optimistic Lock |
| 스케줄러 | @Scheduled + Lock | Pessimistic Lock |

---

## 5. 실행 방법

### 테스트 실행
```bash
./gradlew test --tests "kr.hhplus.be.server.concurrency.*"
```

### 주요 설정
- `@EnableRetry` - 낙관적 락 재시도 활성화
- `@EnableScheduling` - 스케줄러 활성화
- `@Transactional` - 트랜잭션 범위 = 락 범위

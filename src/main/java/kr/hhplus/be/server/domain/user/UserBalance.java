package kr.hhplus.be.server.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserBalance {
    private Long id;
    private String userId;
    private int balance;
    private LocalDateTime updatedAt;

    public static UserBalance create(String userId, int initialBalance) {
        return new UserBalance(null, userId, initialBalance, LocalDateTime.now());
    }

    public void charge(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
        this.balance += amount;
        this.updatedAt = LocalDateTime.now();
    }

    public void use(int amount) {
        if (amount > balance) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }
        this.balance -= amount;
        this.updatedAt = LocalDateTime.now();
    }
}

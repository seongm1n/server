package kr.hhplus.be.server.infrastructure.persistence.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_balance")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserBalanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private int balance;

    @Version
    private Long version;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public static UserBalanceEntity from(kr.hhplus.be.server.domain.user.UserBalance userBalance) {
        return new UserBalanceEntity(
            userBalance.getId(),
            userBalance.getUserId(),
            userBalance.getBalance(),
            userBalance.getVersion(),
            userBalance.getUpdatedAt()
        );
    }
    
    public kr.hhplus.be.server.domain.user.UserBalance toDomain() {
        return new kr.hhplus.be.server.domain.user.UserBalance(
            this.id,
            this.userId,
            this.balance,
            this.version,
            this.updatedAt
        );
    }
    
    public void updateBalance(int balance, LocalDateTime updatedAt) {
        this.balance = balance;
        this.updatedAt = updatedAt;
    }
}

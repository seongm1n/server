package kr.hhplus.be.server.application;

import kr.hhplus.be.server.application.dto.PaymentResult;
import kr.hhplus.be.server.application.usecase.payment.PaymentUseCase;
import kr.hhplus.be.server.domain.payment.*;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueStatus;
import kr.hhplus.be.server.domain.reservation.*;
import kr.hhplus.be.server.domain.user.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("결제 UseCase 테스트")
class PaymentUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private UserBalanceRepository userBalanceRepository;
    
    @Mock
    private SeatRepository seatRepository;
    
    @Mock
    private QueueTokenRepository queueTokenRepository;

    private PaymentUseCase paymentUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentUseCase = new PaymentUseCase(paymentRepository, reservationRepository, 
                                          userBalanceRepository, seatRepository, queueTokenRepository);
    }

    @Test
    @DisplayName("예약을 결제할 수 있다")
    void payForReservation() {
        String userId = "user123";
        Long reservationId = 1L;
        int price = 50000;
        
        QueueToken queueToken = new QueueToken(1L, userId, "token", 1, QueueStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
        Reservation reservation = new Reservation(reservationId, userId, 1L, price,
                ReservationStatus.PENDING, null);
        UserBalance userBalance = UserBalance.create(userId, 100000);
        Seat seat = new Seat(1L, 1L, 1, price, SeatStatus.TEMPORARILY_RESERVED, 
                           userId, null);
        
        when(queueTokenRepository.findActiveByUserId(userId)).thenReturn(Optional.of(queueToken));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userBalanceRepository.findByUserId(userId)).thenReturn(Optional.of(userBalance));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
        when(paymentRepository.save(any(Payment.class)))
            .thenAnswer(invocation -> {
                Payment payment = invocation.getArgument(0);
                return new Payment(1L, payment.getUserId(), payment.getReservationId(),
                                 payment.getAmount(), payment.getStatus(), payment.getCreatedAt(),
                                 payment.getCompletedAt(), payment.getFailureReason());
            });

        PaymentResult result = paymentUseCase.pay(userId, reservationId);

        assertThat(result.getPaymentId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        
        verify(userBalanceRepository).save(userBalance);
        verify(reservationRepository).save(reservation);
        verify(seatRepository).save(seat);
        verify(paymentRepository).save(any(Payment.class));
        
        assertThat(userBalance.getBalance()).isEqualTo(50000);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.CONFIRMED);
    }

    @Test
    @DisplayName("존재하지 않는 예약은 결제할 수 없다")
    void cannotPayForNonExistentReservation() {
        String userId = "user123";
        Long reservationId = 1L;
        
        QueueToken queueToken = new QueueToken(1L, userId, "token", 1, QueueStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
        
        when(queueTokenRepository.findActiveByUserId(userId)).thenReturn(Optional.of(queueToken));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentUseCase.pay(userId, reservationId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 예약입니다.");
    }

    @Test
    @DisplayName("잔액이 부족하면 결제에 실패한다")
    void failPaymentWhenInsufficientBalance() {
        String userId = "user123";
        Long reservationId = 1L;
        int price = 50000;
        
        QueueToken queueToken = new QueueToken(1L, userId, "token", 1, QueueStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
        Reservation reservation = new Reservation(reservationId, userId, 1L, price,
                ReservationStatus.PENDING, null);
        UserBalance userBalance = UserBalance.create(userId, 30000);
        Seat seat = new Seat(1L, 1L, 1, price, SeatStatus.TEMPORARILY_RESERVED, 
                           userId, null);
        
        when(queueTokenRepository.findActiveByUserId(userId)).thenReturn(Optional.of(queueToken));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(userBalanceRepository.findByUserId(userId)).thenReturn(Optional.of(userBalance));
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));

        assertThatThrownBy(() -> paymentUseCase.pay(userId, reservationId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("잔액이 부족합니다.");
    }

    @Test
    @DisplayName("예약자와 결제자가 다르면 결제에 실패한다")
    void failPaymentWhenReservationUserAndPayerAreDifferent() {
        String reservationOwner = "user123";
        String payer = "user456";
        Long reservationId = 1L;
        int price = 50000;
        
        QueueToken payerQueueToken = new QueueToken(1L, payer, "token", 1, QueueStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
        Reservation reservation = new Reservation(reservationId, reservationOwner, 1L, price,
                ReservationStatus.PENDING, null);
        
        when(queueTokenRepository.findActiveByUserId(payer)).thenReturn(Optional.of(payerQueueToken));
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> paymentUseCase.pay(payer, reservationId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("예약자와 결제자가 일치하지 않습니다.");
        
        verify(userBalanceRepository, never()).findByUserId(any());
        verify(seatRepository, never()).findById(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("QueueToken이 만료되면 결제에 실패한다")
    void failPaymentWhenQueueTokenExpired() {
        String userId = "user123";
        Long reservationId = 1L;
        int price = 50000;
        
        QueueToken expiredToken = new QueueToken(1L, userId, "token", 1, QueueStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now().minusMinutes(1)); // 1분 전에 만료
        
        when(queueTokenRepository.findActiveByUserId(userId)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> paymentUseCase.pay(userId, reservationId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("대기열 토큰이 만료되었습니다.");
        
        verify(queueTokenRepository).save(expiredToken);
        verify(reservationRepository, never()).findById(any());
    }
}

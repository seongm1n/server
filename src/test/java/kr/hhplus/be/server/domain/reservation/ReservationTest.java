package kr.hhplus.be.server.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("예약 도메인 테스트")
class ReservationTest {

    @Test
    @DisplayName("예약을 생성할 수 있다")
    void createReservation() {
        String userId = "user123";
        Long seatId = 1L;
        int price = 50000;

        Reservation reservation = Reservation.create(userId, seatId, price);

        assertThat(reservation.getUserId()).isEqualTo(userId);
        assertThat(reservation.getSeatId()).isEqualTo(seatId);
        assertThat(reservation.getPrice()).isEqualTo(price);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(reservation.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("예약을 확정할 수 있다")
    void confirmReservation() {
        Reservation reservation = Reservation.create("user123", 1L, 50000);

        reservation.confirm();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("예약을 취소할 수 있다")
    void cancelReservation() {
        Reservation reservation = Reservation.create("user123", 1L, 50000);

        reservation.cancel();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

}

package kr.hhplus.be.server.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("좌석 도메인 테스트")
class SeatTest {

    @Test
    @DisplayName("좌석을 생성할 수 있다")
    void createSeat() {
        Long concertScheduleId = 1L;
        int seatNumber = 1;
        int price = 50000;

        Seat seat = Seat.create(concertScheduleId, seatNumber, price);

        assertThat(seat.getConcertScheduleId()).isEqualTo(concertScheduleId);
        assertThat(seat.getSeatNumber()).isEqualTo(seatNumber);
        assertThat(seat.getPrice()).isEqualTo(price);
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("좌석을 임시 예약할 수 있다")
    void reserveSeat() {
        Seat seat = Seat.create(1L, 1, 50000);
        String userId = "user123";

        seat.reserve(userId);

        assertThat(seat.getStatus()).isEqualTo(SeatStatus.TEMPORARILY_RESERVED);
        assertThat(seat.getReservedBy()).isEqualTo(userId);
        assertThat(seat.getReservedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("이미 예약된 좌석은 예약할 수 없다")
    void cannotReserveAlreadyReservedSeat() {
        Seat seat = Seat.create(1L, 1, 50000);
        seat.reserve("user1");

        assertThatThrownBy(() -> seat.reserve("user2"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 예약된 좌석입니다.");
    }

    @Test
    @DisplayName("임시 예약된 좌석을 확정할 수 있다")
    void confirmReservation() {
        Seat seat = Seat.create(1L, 1, 50000);
        seat.reserve("user123");

        seat.confirm();

        assertThat(seat.getStatus()).isEqualTo(SeatStatus.CONFIRMED);
    }

    @Test
    @DisplayName("임시 예약된 좌석을 해제할 수 있다")
    void releaseSeat() {
        Seat seat = Seat.create(1L, 1, 50000);
        seat.reserve("user123");

        seat.release();

        assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(seat.getReservedBy()).isNull();
        assertThat(seat.getReservedAt()).isNull();
    }

}

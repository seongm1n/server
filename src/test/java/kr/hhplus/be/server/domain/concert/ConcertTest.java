package kr.hhplus.be.server.domain.concert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.*;

@DisplayName("콘서트 도메인 테스트")
class ConcertTest {

    @Test
    @DisplayName("콘서트를 생성할 수 있다")
    void createConcert() {
        String title = "아이유 콘서트";

        Concert concert = Concert.create(title);

        assertThat(concert.getTitle()).isEqualTo(title);
        assertThat(concert.getCreatedAt()).isNotNull();
    }

}

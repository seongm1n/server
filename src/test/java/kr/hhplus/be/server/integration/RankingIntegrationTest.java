package kr.hhplus.be.server.integration;

import kr.hhplus.be.server.application.event.SeatReservedEvent;
import kr.hhplus.be.server.application.usecase.ranking.RankingUseCase;
import kr.hhplus.be.server.domain.ranking.ConcertRanking;
import kr.hhplus.be.server.infrastructure.persistence.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RankingIntegrationTest extends TestContainerConfig {

    @Autowired
    private RankingUseCase rankingUseCase;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.delete("ranking:fast-sellout");
    }

    @Test
    @DisplayName("예약 이벤트 발행 시 랭킹에 반영")
    void rankingUpdatedOnReservation() {
        Long concertId = 1L;

        for (int i = 0; i < 3; i++) {
            rankingUseCase.handleSeatReservedEvent(new SeatReservedEvent(concertId));
        }

        List<ConcertRanking> rankings = rankingUseCase.getFastSelloutRanking(10);
        assertThat(rankings).hasSize(1);
        assertThat(rankings.get(0).getConcertScheduleId()).isEqualTo(concertId);
        assertThat(rankings.get(0).getReservedCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("여러 콘서트 랭킹 정렬 확인")
    void multipleConceptRanking() {
        rankingUseCase.handleSeatReservedEvent(new SeatReservedEvent(1L));

        rankingUseCase.handleSeatReservedEvent(new SeatReservedEvent(2L));
        rankingUseCase.handleSeatReservedEvent(new SeatReservedEvent(2L));
        rankingUseCase.handleSeatReservedEvent(new SeatReservedEvent(2L));

        rankingUseCase.handleSeatReservedEvent(new SeatReservedEvent(3L));
        rankingUseCase.handleSeatReservedEvent(new SeatReservedEvent(3L));

        List<ConcertRanking> rankings = rankingUseCase.getFastSelloutRanking(10);
        assertThat(rankings).hasSize(3);
        assertThat(rankings.get(0).getConcertScheduleId()).isEqualTo(2L);
        assertThat(rankings.get(1).getConcertScheduleId()).isEqualTo(3L);
        assertThat(rankings.get(2).getConcertScheduleId()).isEqualTo(1L);
    }
}

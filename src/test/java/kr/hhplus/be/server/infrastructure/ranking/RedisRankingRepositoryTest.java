package kr.hhplus.be.server.infrastructure.ranking;

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
class RedisRankingRepositoryTest extends TestContainerConfig {

    @Autowired
    private RedisRankingRepository rankingRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.delete("ranking:fast-sellout");
    }

    @Test
    @DisplayName("점수 증가 테스트")
    void incrementScore() {
        Long concertId = 1L;

        rankingRepository.incrementScore(concertId);
        rankingRepository.incrementScore(concertId);
        rankingRepository.incrementScore(concertId);

        List<ConcertRanking> rankings = rankingRepository.getTopRankings(10);
        assertThat(rankings).hasSize(1);
        assertThat(rankings.get(0).getConcertScheduleId()).isEqualTo(concertId);
        assertThat(rankings.get(0).getReservedCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("상위 N개 조회 - 점수 높은 순")
    void getTopRankings() {
        rankingRepository.incrementScore(1L);

        rankingRepository.incrementScore(2L);
        rankingRepository.incrementScore(2L);
        rankingRepository.incrementScore(2L);

        rankingRepository.incrementScore(3L);
        rankingRepository.incrementScore(3L);

        List<ConcertRanking> rankings = rankingRepository.getTopRankings(10);

        assertThat(rankings).hasSize(3);
        assertThat(rankings.get(0).getConcertScheduleId()).isEqualTo(2L);
        assertThat(rankings.get(0).getReservedCount()).isEqualTo(3);
        assertThat(rankings.get(1).getConcertScheduleId()).isEqualTo(3L);
        assertThat(rankings.get(2).getConcertScheduleId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("limit만큼만 조회")
    void getTopRankings_WithLimit() {
        for (long i = 1; i <= 20; i++) {
            rankingRepository.incrementScore(i);
        }

        List<ConcertRanking> rankings = rankingRepository.getTopRankings(5);

        assertThat(rankings).hasSize(5);
    }
}

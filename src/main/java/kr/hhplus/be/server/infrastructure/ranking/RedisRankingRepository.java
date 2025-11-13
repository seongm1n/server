package kr.hhplus.be.server.infrastructure.ranking;

import kr.hhplus.be.server.domain.ranking.ConcertRanking;
import kr.hhplus.be.server.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RedisRankingRepository implements RankingRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String RANKING_KEY = "ranking:fast-sellout";

    @Override
    public void incrementScore(Long concertScheduleId) {
        redisTemplate.opsForZSet().incrementScore(
            RANKING_KEY,
            concertScheduleId.toString(),
            1.0
        );
    }

    @Override
    public List<ConcertRanking> getTopRankings(int limit) {
        Set<TypedTuple<Object>> result = redisTemplate.opsForZSet()
            .reverseRangeWithScores(RANKING_KEY, 0, limit - 1);

        if (result == null) {
            return List.of();
        }

        List<ConcertRanking> rankings = new ArrayList<>();
        int rank = 1;
        for (TypedTuple<Object> tuple : result) {
            Long concertId = Long.parseLong(tuple.getValue().toString());
            int score = tuple.getScore().intValue();
            rankings.add(new ConcertRanking(concertId, score, rank++));
        }

        return rankings;
    }
}

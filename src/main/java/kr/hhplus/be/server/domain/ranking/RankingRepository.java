package kr.hhplus.be.server.domain.ranking;

import java.util.List;

public interface RankingRepository {
    void incrementScore(Long concertScheduleId);
    List<ConcertRanking> getTopRankings(int limit);
}

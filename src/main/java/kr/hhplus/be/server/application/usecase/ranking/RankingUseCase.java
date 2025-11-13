package kr.hhplus.be.server.application.usecase.ranking;

import kr.hhplus.be.server.application.event.SeatReservedEvent;
import kr.hhplus.be.server.domain.ranking.ConcertRanking;
import kr.hhplus.be.server.domain.ranking.RankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingUseCase {

    private final RankingRepository rankingRepository;

    public List<ConcertRanking> getFastSelloutRanking(int limit) {
        return rankingRepository.getTopRankings(limit);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSeatReservedEvent(SeatReservedEvent event) {
        rankingRepository.incrementScore(event.getConcertScheduleId());
        log.info("Ranking updated: concertScheduleId={}", event.getConcertScheduleId());
    }
}

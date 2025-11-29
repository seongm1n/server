package kr.hhplus.be.server.application;

import kr.hhplus.be.server.application.event.SeatReservedEvent;
import kr.hhplus.be.server.application.usecase.ranking.RankingUseCase;
import kr.hhplus.be.server.domain.ranking.ConcertRanking;
import kr.hhplus.be.server.domain.ranking.RankingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingUseCaseTest {

    @Mock
    private RankingRepository rankingRepository;

    @InjectMocks
    private RankingUseCase rankingUseCase;

    @Test
    @DisplayName("좌석 예약 이벤트 발생 시 랭킹 업데이트")
    void handleSeatReservedEvent() {
        SeatReservedEvent event = new SeatReservedEvent(100L);

        rankingUseCase.handleSeatReservedEvent(event);

        verify(rankingRepository, times(1)).incrementScore(100L);
    }

    @Test
    @DisplayName("상위 랭킹 조회")
    void getFastSelloutRanking() {
        List<ConcertRanking> mockRankings = List.of(
            new ConcertRanking(1L, 100, 1),
            new ConcertRanking(2L, 80, 2)
        );
        when(rankingRepository.getTopRankings(10)).thenReturn(mockRankings);

        List<ConcertRanking> result = rankingUseCase.getFastSelloutRanking(10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRank()).isEqualTo(1);
    }
}

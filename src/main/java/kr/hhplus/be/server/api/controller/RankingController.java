package kr.hhplus.be.server.api.controller;

import kr.hhplus.be.server.api.dto.RankingResponse;
import kr.hhplus.be.server.application.usecase.ranking.RankingUseCase;
import kr.hhplus.be.server.domain.ranking.ConcertRanking;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingUseCase rankingUseCase;

    @GetMapping("/fast-sellout")
    public ResponseEntity<RankingResponse> getFastSelloutRanking(
        @RequestParam(defaultValue = "10") int limit
    ) {
        List<ConcertRanking> rankings = rankingUseCase.getFastSelloutRanking(limit);

        List<RankingResponse.RankingItem> items = rankings.stream()
            .map(r -> new RankingResponse.RankingItem(
                r.getRank(),
                r.getConcertScheduleId(),
                r.getReservedCount()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(new RankingResponse(items));
    }
}

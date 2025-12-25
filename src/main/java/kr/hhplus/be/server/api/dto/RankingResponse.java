package kr.hhplus.be.server.api.dto;

import java.util.List;

public record RankingResponse(
    List<RankingItem> rankings
) {
    public record RankingItem(
        int rank,
        Long concertScheduleId,
        int reservedCount
    ) {}
}

package kr.hhplus.be.server.domain.ranking;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConcertRanking {
    private Long concertScheduleId;
    private int reservedCount;
    private int rank;
}

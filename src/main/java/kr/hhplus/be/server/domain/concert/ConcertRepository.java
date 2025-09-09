package kr.hhplus.be.server.domain.concert;

import java.time.LocalDate;
import java.util.List;

public interface ConcertRepository {
    List<LocalDate> getAvailableDates(Long concertId);
}

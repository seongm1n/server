package kr.hhplus.be.server.domain.concert;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class Concert {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private List<ConcertSchedule> schedules;

    public static Concert create(String title) {
        return new Concert(null, title, LocalDateTime.now(), new ArrayList<>());
    }
}

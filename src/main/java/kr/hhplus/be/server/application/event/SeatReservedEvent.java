package kr.hhplus.be.server.application.event;

public class SeatReservedEvent {
    private final Long concertScheduleId;

    public SeatReservedEvent(Long concertScheduleId) {
        this.concertScheduleId = concertScheduleId;
    }

    public Long getConcertScheduleId() {
        return concertScheduleId;
    }
}

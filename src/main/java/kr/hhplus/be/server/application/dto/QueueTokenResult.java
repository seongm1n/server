package kr.hhplus.be.server.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QueueTokenResult {
    private String token;
    private int queuePosition;
    private String status;
}

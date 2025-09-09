package kr.hhplus.be.server.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentResult {
    private Long paymentId;
    private String status;
}

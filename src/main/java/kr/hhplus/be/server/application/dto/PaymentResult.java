package kr.hhplus.be.server.application.dto;

import kr.hhplus.be.server.domain.payment.PaymentStatus;
import lombok.Getter;

@Getter
public class PaymentResult {
    private Long paymentId;
    private String statusString;
    
    public PaymentResult(Long paymentId, String status) {
        this.paymentId = paymentId;
        this.statusString = status;
    }
    
    public PaymentStatus getStatus() {
        return PaymentStatus.valueOf(statusString);
    }
}

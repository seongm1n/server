package kr.hhplus.be.server.api.controller;

import kr.hhplus.be.server.api.dto.PaymentRequest;
import kr.hhplus.be.server.application.dto.PaymentResult;
import kr.hhplus.be.server.application.usecase.payment.PaymentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    @PostMapping
    public ResponseEntity<PaymentResult> pay(@RequestBody PaymentRequest request) {
        PaymentResult result = paymentUseCase.pay(request.userId(), request.reservationId());
        return ResponseEntity.ok(result);
    }
}

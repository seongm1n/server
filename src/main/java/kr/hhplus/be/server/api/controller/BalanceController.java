package kr.hhplus.be.server.api.controller;

import kr.hhplus.be.server.api.dto.ChargeRequest;
import kr.hhplus.be.server.application.dto.BalanceResult;
import kr.hhplus.be.server.application.usecase.balance.BalanceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceUseCase balanceUseCase;

    @PostMapping("/charge")
    public ResponseEntity<BalanceResult> charge(@RequestBody ChargeRequest request) {
        BalanceResult result = balanceUseCase.charge(request.userId(), request.amount());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<BalanceResult> getBalance(@PathVariable String userId) {
        BalanceResult result = balanceUseCase.getBalance(userId);
        return ResponseEntity.ok(result);
    }
}

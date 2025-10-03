package kr.hhplus.be.server.api.controller;

import kr.hhplus.be.server.api.dto.TokenRequest;
import kr.hhplus.be.server.application.dto.QueueTokenResult;
import kr.hhplus.be.server.application.usecase.queue.QueueUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueUseCase queueUseCase;

    @PostMapping("/token")
    public ResponseEntity<QueueTokenResult> issueToken(@RequestBody TokenRequest request) {
        QueueTokenResult result = queueUseCase.issueToken(request.userId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    public ResponseEntity<QueueTokenResult> getQueueStatus(@RequestParam String token) {
        QueueTokenResult result = queueUseCase.getQueueStatus(token);
        return ResponseEntity.ok(result);
    }
}

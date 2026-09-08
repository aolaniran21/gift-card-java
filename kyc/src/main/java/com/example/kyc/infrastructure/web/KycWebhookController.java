package com.example.kyc.infrastructure.web;

import com.example.kyc.application.KycService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;

@RestController
@RequestMapping("/api/kyc/provider")
public class KycWebhookController {
    private final KycService kycService;
    private final String sharedSecret = "phase2-kyc-secret"; // override via config in production

    public KycWebhookController(KycService kycService) {
        this.kycService = kycService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(HttpServletRequest request, @RequestHeader(value = "X-Signature", required = false) String signature) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        String payload = sb.toString();
        // verify signature - stubbed to check non-null for Phase 2
        if (signature == null || signature.isBlank()) {
            return ResponseEntity.badRequest().body("missing signature");
        }
        // Delegate to application service which should handle idempotency and async processing
        kycService.handleProviderCallback(payload);
        return ResponseEntity.ok("received");
    }
}

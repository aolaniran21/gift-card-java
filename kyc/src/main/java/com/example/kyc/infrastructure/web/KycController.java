package com.example.kyc.infrastructure.web;

import com.example.kyc.application.KycService;
import com.example.kyc.domain.KycSubmission;
import com.example.shared.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kyc")
public class KycController {
    private final KycService kycService;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    public static record KycRequest(@NotBlank String data) {}

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<KycSubmission>> submit(@Valid @RequestBody KycRequest req) {
        KycSubmission s = kycService.submitKyc(0L, req.data()); // userId wiring to be implemented
        return ResponseEntity.ok(new ApiResponse<>("Submitted", s));
    }
}

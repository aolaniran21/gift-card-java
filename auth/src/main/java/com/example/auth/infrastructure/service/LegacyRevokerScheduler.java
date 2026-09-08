package com.example.auth.infrastructure.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "auth.legacy-revoker", name = "enabled", havingValue = "true", matchIfMissing = false)
public class LegacyRevokerScheduler {

    private final LegacyRefreshTokenRevoker revoker;

    // ttlDays and cron can be configured via application properties
    private final int ttlDays;

    public LegacyRevokerScheduler(LegacyRefreshTokenRevoker revoker,
                                  org.springframework.core.env.Environment env) {
        this.revoker = revoker;
        this.ttlDays = Integer.parseInt(env.getProperty("auth.legacy-revoker.ttl-days", "30"));
    }

    // runs daily at midnight by default (cron configurable via auth.legacy-revoker.cron)
    @Scheduled(cron = "${auth.legacy-revoker.cron:0 0 0 * * *}")
    public void run() {
        int count = revoker.revokeLegacyTokensOlderThanDays(ttlDays);
        // simple log
        System.out.println("LegacyRevokerScheduler: revoked " + count + " legacy tokens older than " + ttlDays + " days");
    }
}

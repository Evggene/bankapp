package org.bea.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AccountsMetrics {

    private final MeterRegistry registry;

    public AccountsMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordSignup(boolean success) {
        Counter.builder("accounts_signup_total")
                .description("Signup attempts in Accounts service")
                .tag("result", success ? "success" : "failure")
                .register(registry)
                .increment();
    }

}

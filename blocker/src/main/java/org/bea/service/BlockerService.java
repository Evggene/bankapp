package org.bea.service;

import org.bea.dto.BlockRequest;
import org.bea.dto.BlockResponse;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BlockerService {

    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    public BlockResponse decide(BlockRequest req) {
        String login = req.getLogin() == null ? "anonymous" : req.getLogin();
        String action = req.getAction() == null ? "UNKNOWN" : req.getAction().toUpperCase();
        String key = login + "|" + action;

        int current = counters.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
        boolean allowed = (current % 10 != 0);
        String reason = allowed ? "allowed (odd attempt #" + current + ")" : "blocked (even attempt #" + current + ")";
        return new BlockResponse(allowed, reason, current);
    }
}

package org.bea.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RestController
@RequestMapping
public class NotificationController {

    // In-memory FIFO queue of messages (no DB as requested).
    private final Queue<String> messages = new ConcurrentLinkedQueue<>();

    /**
     * Accept notifications from other services (cash, transfer).
     * Works with either form-urlencoded or JSON. Use ?operation=... for simplest calls.
     */
    @PostMapping(value = "/notify", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE })
    public ResponseEntity<Void> push(@RequestParam(name = "operation", required = false) String operation,
                                     @RequestBody(required = false) String body) {
        // If operation not provided as a param, try reading entire body as message.
        String op = (operation != null && !operation.isBlank()) ? operation : (body == null ? "" : body.trim());
        if (!op.isBlank()) {
            messages.offer(op);
        }
        return ResponseEntity.accepted().build();
    }

    /**
     * Simplest poll endpoint for the front-end.
     * Returns and removes the next pending message, or empty 204 if none.
     */
    @GetMapping("/last")
    public ResponseEntity<String> pull() {
        String msg = messages.poll();
        if (msg == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(msg);
    }
}

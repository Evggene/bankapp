package org.bea.controller;

import lombok.RequiredArgsConstructor;
import org.bea.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RestController
@RequestMapping
public class NotificationController {

    @PostMapping(value = "/notify", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE })
    public ResponseEntity<Void> push(@RequestParam(name = "operation", required = false) String operation,
                                     @RequestBody(required = false) String body) {
        // If operation not provided as a param, try reading entire body as message.
        String op = (operation != null && !operation.isBlank()) ? operation : (body == null ? "" : body.trim());
        if (!op.isBlank()) {
            NotificationService.messages.offer(op);
        }
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/last")
    public ResponseEntity<String> pull() {
        String msg = NotificationService.messages.poll();
        if (msg == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(msg);
    }
}

package org.bea.controller;

import lombok.RequiredArgsConstructor;
import org.bea.dto.BlockRequest;
import org.bea.dto.BlockResponse;
import org.bea.service.BlockerService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/check")
@RequiredArgsConstructor
public class BlockerController {

    private final BlockerService service;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public BlockResponse check(@RequestBody BlockRequest req) {
        return service.decide(req);
    }
}

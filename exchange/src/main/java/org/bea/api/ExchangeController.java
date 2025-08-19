package org.bea.api;

import lombok.RequiredArgsConstructor;
import org.bea.api.dto.ConversionRequest;
import org.bea.api.dto.ConversionResponse;
import org.bea.repo.ConversionOperationRepository;
import org.bea.service.ExchangeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService service;
    private final ConversionOperationRepository repo;

    @PostMapping("/convert")
    public ConversionResponse convert(@RequestBody ConversionRequest request) {
        return service.convert(request);
    }

    @GetMapping("/operations")
    public List<?> list() {
        return repo.findAll().stream()
                .sorted((a,b) -> b.getTs().compareTo(a.getTs()))
                .limit(50)
                .toList();
    }
}

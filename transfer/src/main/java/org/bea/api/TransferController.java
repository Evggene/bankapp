package org.bea.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bea.api.dto.TransferFormRequest;
import org.bea.repo.TransferOperationRepository;
import org.bea.service.TransferService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TransferController {

    private final TransferService svc;
    private final TransferOperationRepository repo;

    @PostMapping(value = "/user/{login}/doTransfer",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> doTransfer(@PathVariable String login, @Valid TransferFormRequest form) {
        UUID id = svc.transfer(
                login,
                form.getTo_login(),
                form.getFrom_currency(),
                form.getTo_currency(),
                form.getValue());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/operations")
    public List<?> lastOperations() {
        return repo.findTop50ByOrderByTsDesc();
    }
}

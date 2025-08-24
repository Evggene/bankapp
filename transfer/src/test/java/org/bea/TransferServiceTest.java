package org.bea;

import org.bea.config.SharedAppProperties;
import org.bea.domain.TransferOperation;
import org.bea.repo.TransferOperationRepository;
import org.bea.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for TransferService: external HTTP calls and persistence are mocked.
 */
public class TransferServiceTest {

    private RestTemplate restTemplate;
    private TransferOperationRepository repo;
    private SharedAppProperties props;
    private TransferService service;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        repo = mock(TransferOperationRepository.class);
        props = mock(SharedAppProperties.class);
        when(props.getGatewayBaseUrl()).thenReturn("http://gateway");
        service = new TransferService(restTemplate, repo, props);
    }

    @Test
    void transfer_allowed_persistsOk_and_callsCash() {
        // Blocker allows
        Map<String, Object> blockerResp = new HashMap<>();
        blockerResp.put("allowed", true);
        when(restTemplate.postForObject(eq("http://gateway/blocker/check"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(blockerResp);

        // Exchange conversion
        Map<String, Object> exchangeResp = new HashMap<>();
        exchangeResp.put("resultAmount", new BigDecimal("720.50"));
        when(restTemplate.postForObject(eq("http://gateway/exchange/convert"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(exchangeResp);

        // Cash withdraw + deposit
        when(restTemplate.postForEntity(startsWith("http://gateway/cash/user/john/getCash"), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
        when(restTemplate.postForEntity(startsWith("http://gateway/cash/user/mary/getCash"), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));

        // Repo save should return entity with id
        ArgumentCaptor<TransferOperation> opCaptor = ArgumentCaptor.forClass(TransferOperation.class);
        when(repo.save(opCaptor.capture())).thenAnswer(inv -> {
            TransferOperation op = opCaptor.getValue();
            if (op.getId() == null) {
                op.setId(UUID.randomUUID());
            }
            return op;
        });

        UUID id = service.transfer("john", "mary", "USD", "CNY", new BigDecimal("100.00"));

        assertThat(id).isNotNull();
        TransferOperation saved = opCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo("OK");
        assertThat(saved.getConvertedAmount()).isEqualByComparingTo("720.50");

        // Verify cash calls were made twice (withdraw + deposit)
        verify(restTemplate, times(1)).postForEntity(startsWith("http://gateway/cash/user/john/getCash"), any(), eq(Void.class));
        verify(restTemplate, times(1)).postForEntity(startsWith("http://gateway/cash/user/mary/getCash"), any(), eq(Void.class));
    }

    @Test
    void transfer_blocked_persistsBlocked_and_throws409() {
        // Blocker denies
        Map<String, Object> blockerResp = new HashMap<>();
        blockerResp.put("allowed", false);
        when(restTemplate.postForObject(eq("http://gateway/blocker/check"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(blockerResp);

        // Repo save capture
        ArgumentCaptor<TransferOperation> opCaptor = ArgumentCaptor.forClass(TransferOperation.class);
        when(repo.save(opCaptor.capture())).thenAnswer(inv -> opCaptor.getValue());

        assertThatThrownBy(() -> service.transfer("john", "mary", "USD", "CNY", new BigDecimal("100.00")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");

        TransferOperation saved = opCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo("BLOCKED");
        assertThat(saved.getBlockerReason()).contains("Operation blocked");
        // Ensure no exchange/cash calls
        verify(restTemplate, never()).postForObject(eq("http://gateway/exchange/convert"), any(), eq(Map.class));
        verify(restTemplate, never()).postForEntity(startsWith("http://gateway/cash/"), any(), eq(Void.class));
    }
}

package org.bea;

import org.bea.config.SharedAppProperties;
import org.bea.domain.CashAccount;
import org.bea.domain.dto.CashBalanceResponse;
import org.bea.repository.CashAccountRepository;
import org.bea.service.CashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CashServiceTest {

    private CashAccountRepository repo;
    private RestTemplate rest;
    private CashService service;
    private SharedAppProperties sharedAppProperties;

    @BeforeEach
    void setUp() {
        sharedAppProperties = mock(SharedAppProperties.class);
        repo = mock(CashAccountRepository.class);
        rest = mock(RestTemplate.class);
        service = new CashService(sharedAppProperties, repo, rest);
    }

    private void mockBlockerAllowed(boolean allowed) {
        Map<String,Object> resp = new HashMap<>();
        resp.put("allowed", allowed);
        when(rest.postForObject(any(String.class), any(), eq(Map.class))).thenReturn(resp);
    }

    @Test
    void deposit_createsAccountIfAbsent_andIncreasesBalance() {
        mockBlockerAllowed(true);

        when(repo.findByUsernameAndCurrency("john", "USD"))
                .thenReturn(Optional.empty());
        // при создании новый аккаунт сохраняется 2 раза (создание и после изменения баланса)
        when(repo.save(any(CashAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        CashBalanceResponse res = service.deposit("john", "USD", new BigDecimal("50.00"));

        assertThat(res.getLogin()).isEqualTo("john");
        assertThat(res.getCurrency()).isEqualTo("USD");
        assertThat(res.getBalance()).isEqualByComparingTo("50.00");
        assertThat(res.getMessage()).isEqualTo("Deposited");

        // проверим, что баланс действительно стал 50
        ArgumentCaptor<CashAccount> captor = ArgumentCaptor.forClass(CashAccount.class);
        verify(repo, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().get(captor.getAllValues().size()-1).getBalance())
                .isEqualByComparingTo("50.00");
    }

    @Test
    void deposit_blockedByPolicy_throws409() {
        mockBlockerAllowed(false);
        assertThatThrownBy(() -> service.deposit("john", "USD", new BigDecimal("10")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("409");
        verify(repo, never()).save(any());
    }

    @Test
    void withdraw_decreasesBalance_whenEnoughMoney() {
        mockBlockerAllowed(true);
        CashAccount acc = CashAccount.builder()
                .username("mary").currency("EUR").balance(new BigDecimal("100.00")).build();
        when(repo.findByUsernameAndCurrency("mary", "EUR")).thenReturn(Optional.of(acc));
        when(repo.save(any(CashAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        CashBalanceResponse res = service.withdraw("mary", "EUR", new BigDecimal("40.00"));

        assertThat(res.getBalance()).isEqualByComparingTo("60.00");
        assertThat(res.getMessage()).isEqualTo("Withdrawn");
    }

    @Test
    void withdraw_notEnoughMoney_throws() {
        mockBlockerAllowed(true);
        CashAccount acc = CashAccount.builder()
                .username("mary").currency("EUR").balance(new BigDecimal("10.00")).build();
        when(repo.findByUsernameAndCurrency("mary", "EUR")).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> service.withdraw("mary", "EUR", new BigDecimal("40.00")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Недостаточно средств");
        verify(repo, never()).save(any());
    }

    @Test
    void withdraw_accountNotFound_throws() {
        mockBlockerAllowed(true);
        when(repo.findByUsernameAndCurrency("ghost", "RUB")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.withdraw("ghost", "RUB", new BigDecimal("1")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Счёт не найден");
    }

    @Test
    void blockerPayload_isPostedToGateway() {
        mockBlockerAllowed(true);
        when(repo.findByUsernameAndCurrency("u", "RUB")).thenReturn(Optional.empty());
        when(repo.save(any(CashAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        service.deposit("u", "RUB", new BigDecimal("1.23"));

        // проверим сам факт вызова; детальный JSON матчить не обязательно
        verify(rest).postForObject(any(String.class), any(), eq(Map.class));
    }
}

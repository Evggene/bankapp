package org.bea.service;

import lombok.RequiredArgsConstructor;
import org.bea.domain.CashAccount;
import org.bea.domain.dto.CashBalanceResponse;
import org.bea.repository.CashAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CashService {

    private final CashAccountRepository repo;

    @Transactional
    public CashBalanceResponse deposit(String login, String currency, BigDecimal amount) {
        CashAccount acc = repo.findByUsernameAndCurrency(login, currency)
                .orElseGet(() -> repo.save(
                        CashAccount.builder()
                                .username(login)
                                .currency(currency)
                                .balance(BigDecimal.ZERO)
                                .build())
                );
        acc.setBalance(acc.getBalance().add(amount));
        repo.save(acc);
        return new CashBalanceResponse(acc.getUsername(), acc.getCurrency(), acc.getBalance(), "Deposited");
    }

    @Transactional
    public CashBalanceResponse withdraw(String login, String currency, BigDecimal amount) {
        CashAccount acc = repo.findByUsernameAndCurrency(login, currency)
                .orElseThrow(() -> new IllegalStateException("Счёт не найден"));
        if (acc.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Недостаточно средств");
        }
        acc.setBalance(acc.getBalance().subtract(amount));
        repo.save(acc);
        return new CashBalanceResponse(acc.getUsername(), acc.getCurrency(), acc.getBalance(), "Withdrawn");
    }
}

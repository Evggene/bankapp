package org.bea.repository;

import org.bea.domain.CashAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CashAccountRepository extends JpaRepository<CashAccount, UUID> {
    Optional<CashAccount> findByUsernameAndCurrency(String username, String currency);
}

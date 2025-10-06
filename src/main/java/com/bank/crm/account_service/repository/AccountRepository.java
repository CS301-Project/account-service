package com.bank.crm.account_service.repository;

import com.bank.crm.account_service.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Find all accounts by client ID
     */
    List<Account> findByClientId(UUID clientId);

    /**
     * Check if account exists by ID
     */
    boolean existsById(UUID id);
}

package org.bea.repo;

import org.bea.domain.TransferOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransferOperationRepository extends JpaRepository<TransferOperation, UUID> {
    List<TransferOperation> findTop50ByOrderByTsDesc();
}

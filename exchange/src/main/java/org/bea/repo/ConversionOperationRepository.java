package org.bea.repo;

import org.bea.domain.ConversionOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConversionOperationRepository extends JpaRepository<ConversionOperation, UUID> {
}

package com.code4ro.legalconsultation.document.consolidated.repository;

import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.user.model.persistence.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentConsolidatedRepository extends JpaRepository<DocumentConsolidated, UUID> {
    Optional<DocumentConsolidated> findByDocumentMetadataId(UUID id);

    Optional<DocumentConsolidated> findByDocumentNodeId(UUID id);

    List<DocumentConsolidated> findAllByAssignedUsersContaining(User user);
}

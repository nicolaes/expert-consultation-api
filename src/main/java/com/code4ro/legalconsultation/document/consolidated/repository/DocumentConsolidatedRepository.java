package com.code4ro.legalconsultation.document.consolidated.repository;

import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.user.model.persistence.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentConsolidatedRepository extends JpaRepository<DocumentConsolidated, UUID> {
    Optional<DocumentConsolidated> findByDocumentMetadataId(UUID id);

    Optional<DocumentConsolidated> findByDocumentNodeId(UUID id);

    List<DocumentConsolidated> findAllByAssignedUsersContaining(User user);

    @Query("SELECT document, document.documentMetadata " +
            "FROM DocumentConsolidated document " +
            "INNER JOIN document.documentConfiguration dc " +
            "WHERE dc.consultationStartDate <= current_date " +
            "AND (dc.consultationDeadline IS NULL OR dc.consultationDeadline >= current_date) " +
            "AND dc.excludedFromConsultation = FALSE " +
            "AND dc.consultationEmailsSent = FALSE")
    List<DocumentConsolidated> findAllInConsultationForEmailNotification();

    @Query("SELECT au " +
            "FROM DocumentConsolidated dc " +
            "INNER JOIN dc.assignedUsers au " +
            "WHERE dc.id = ?1")
    List<User> findUsersByDocumentId(UUID documentId);
}

package com.code4ro.legalconsultation.document.consolidated.repository;

import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.user.model.persistence.User;

import java.util.List;
import java.util.UUID;

public interface QueryDslDocumentConsolidatedRepository {
    List<DocumentConsolidated> findAllInConsultationForEmailNotification();
    List<User> findUsersByDocumentId(UUID documentId);
}

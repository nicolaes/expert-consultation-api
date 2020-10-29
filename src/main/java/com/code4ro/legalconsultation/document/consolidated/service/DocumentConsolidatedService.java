package com.code4ro.legalconsultation.document.consolidated.service;

import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.document.consolidated.repository.DocumentConsolidatedRepository;
import com.code4ro.legalconsultation.document.metadata.model.persistence.DocumentMetadata;
import com.code4ro.legalconsultation.document.node.model.persistence.DocumentNode;
import com.code4ro.legalconsultation.document.node.service.DocumentNodeService;
import com.code4ro.legalconsultation.user.model.persistence.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentConsolidatedService {

    private final DocumentConsolidatedRepository documentConsolidatedRepository;
    private final DocumentNodeService documentNodeService;

    @Autowired
    public DocumentConsolidatedService(final DocumentConsolidatedRepository repository,
                                       final DocumentNodeService documentNodeService) {
        this.documentConsolidatedRepository = repository;
        this.documentNodeService = documentNodeService;
    }

    @Transactional(readOnly = true)
    public DocumentConsolidated getEntity(final UUID id) {
        return documentConsolidatedRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public DocumentConsolidated getByDocumentMetadataId(final UUID id) {
        return documentConsolidatedRepository.
                findByDocumentMetadataId(id).orElseThrow(EntityNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public DocumentConsolidated getByMemberDocumentNodeId(final UUID id) {
        DocumentNode rootNodeForDocument = documentNodeService.findRootNodeForId(id);
        return documentConsolidatedRepository
                .findByDocumentNodeId(rootNodeForDocument.getId())
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<DocumentConsolidated> findAll() {
        return documentConsolidatedRepository.findAll();
    }

    @Transactional
    public DocumentConsolidated saveOne(final DocumentConsolidated documentConsolidated) {
        return documentConsolidatedRepository.save(documentConsolidated);
    }

    @Transactional
    public List<DocumentConsolidated> saveAll(List<DocumentConsolidated> documentConsolidatedList) {
        return documentConsolidatedRepository.saveAll(documentConsolidatedList);
    }

    @Transactional
    public void deleteById(final UUID uuid) {
        documentConsolidatedRepository.deleteById(uuid);
    }

    @Transactional
    public DocumentConsolidated update(final String id,
                                       final DocumentMetadata metadata,
                                       final DocumentNode documentNode) {
        DocumentConsolidated consolidated = documentConsolidatedRepository.getOne(UUID.fromString(id));
        consolidated.setDocumentMetadata(metadata);
        consolidated.setDocumentNode(documentNode);
        return documentConsolidatedRepository.save(consolidated);
    }

    public List<DocumentConsolidated> getAllAssignedDocuments(final User user) {
        return documentConsolidatedRepository.findAllByAssignedUsersContaining(user);
    }
}

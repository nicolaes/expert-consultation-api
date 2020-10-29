package com.code4ro.legalconsultation.document.node.factory;

import com.code4ro.legalconsultation.core.factory.RandomObjectFiller;
import com.code4ro.legalconsultation.document.configuration.model.persistence.DocumentConfiguration;
import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.document.consolidated.repository.DocumentConsolidatedRepository;
import com.code4ro.legalconsultation.document.metadata.model.persistence.DocumentMetadata;
import com.code4ro.legalconsultation.document.metadata.repository.DocumentMetadataRepository;
import com.code4ro.legalconsultation.document.node.model.persistence.DocumentNode;
import com.code4ro.legalconsultation.user.model.persistence.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentFactory {
    @Autowired
    private DocumentConsolidatedRepository documentConsolidatedRepository;
    @Autowired
    private DocumentNodeFactory documentNodeFactory;
    @Autowired
    private DocumentMetadataRepository documentMetadataRepository;

    public DocumentConsolidated create() {
        final DocumentNode rootNode = documentNodeFactory.create();
        final DocumentMetadata metadata = documentMetadataRepository
                .save(RandomObjectFiller.createAndFillWithBaseEntity(DocumentMetadata.class));

        final DocumentConsolidated documentConsolidated = new DocumentConsolidated();
        documentConsolidated.setDocumentNode(rootNode);
        documentConsolidated.setDocumentConfiguration(new DocumentConfiguration());
        documentConsolidated.setDocumentMetadata(metadata);

        return documentConsolidatedRepository.save(documentConsolidated);
    }

}

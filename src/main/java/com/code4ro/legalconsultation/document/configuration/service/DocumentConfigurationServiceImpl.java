package com.code4ro.legalconsultation.document.configuration.service;

import com.code4ro.legalconsultation.document.configuration.model.persistence.DocumentConfiguration;
import com.code4ro.legalconsultation.document.configuration.repository.DocumentConfigurationRepository;
import com.code4ro.legalconsultation.document.consolidated.model.dto.DocumentConsultationDataDto;
import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.document.consolidated.service.DocumentConsolidatedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;

@Service
public class DocumentConfigurationServiceImpl implements DocumentConfigurationService {

    private final DocumentConfigurationRepository documentConfigurationRepository;
    private final DocumentConsolidatedService documentConsolidatedService;

    @Autowired
    public DocumentConfigurationServiceImpl(final DocumentConfigurationRepository repository,
                                            final DocumentConsolidatedService documentConsolidatedService) {
        this.documentConfigurationRepository = repository;
        this.documentConsolidatedService = documentConsolidatedService;
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentConfiguration getEntity(final UUID id) {
        return documentConfigurationRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public DocumentConfiguration saveOne(final DocumentConfiguration documentConfiguration) {
        return documentConfigurationRepository.save(documentConfiguration);
    }

    @Transactional
    @Override
    public void addConsultationData(final UUID metadataId, final DocumentConsultationDataDto consultationDataDto) {
        final DocumentConsolidated documentConsolidated =
                this.documentConsolidatedService.getByDocumentMetadataId(metadataId);
        final DocumentConfiguration documentConfiguration = documentConsolidated.getDocumentConfiguration();
        documentConfiguration.setConsultationStartDate(consultationDataDto.getConsultationStartDate());
        documentConfiguration.setConsultationDeadline(consultationDataDto.getConsultationDeadline());
        documentConfiguration.setExcludedFromConsultation(consultationDataDto.getExcludedFromConsultation());
        this.saveOne(documentConfiguration);
    }

    @Override
    public DocumentConsultationDataDto getConsultationData(final UUID metadataId) {
        final DocumentConsolidated documentConsolidated =
                this.documentConsolidatedService.getByDocumentMetadataId(metadataId);
        final DocumentConfiguration documentConfiguration = documentConsolidated.getDocumentConfiguration();
        return new DocumentConsultationDataDto(documentConfiguration.getConsultationStartDate(),
                documentConfiguration.getConsultationDeadline(), documentConfiguration.getExcludedFromConsultation());
    }
}

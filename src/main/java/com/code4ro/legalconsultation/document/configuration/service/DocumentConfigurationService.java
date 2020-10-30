package com.code4ro.legalconsultation.document.configuration.service;

import com.code4ro.legalconsultation.document.configuration.model.persistence.DocumentConfiguration;
import com.code4ro.legalconsultation.document.consolidated.model.dto.DocumentConsultationDataDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface DocumentConfigurationService {
    @Transactional(readOnly = true)
    DocumentConfiguration getEntity(UUID id);

    /**
     * Adds information about the consultation period of a document
     *
     * @param metadataId                  of the document to be updated
     * @param consultationDataDto consultation start date, deadline and exclusion flag
     */
    void addConsultationData(UUID metadataId, DocumentConsultationDataDto consultationDataDto);

    /**
     * Loads consultation data for a given document id
     *
     * @param id of the document
     * @return consultation data of the document
     */
    DocumentConsultationDataDto getConsultationData(UUID id);

    /**
     * Saves a document configuration
     * @param documentConfiguration .
     * @return saved document configuration
     */
    DocumentConfiguration saveOne(final DocumentConfiguration documentConfiguration);

}

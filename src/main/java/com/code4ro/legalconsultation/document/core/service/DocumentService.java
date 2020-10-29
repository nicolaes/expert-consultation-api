package com.code4ro.legalconsultation.document.core.service;

import com.code4ro.legalconsultation.document.consolidated.model.dto.DocumentConsolidatedDto;
import com.code4ro.legalconsultation.document.consolidated.model.dto.DocumentConsultationDataDto;
import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.document.export.model.DocumentExportFormat;
import com.code4ro.legalconsultation.document.metadata.model.dto.DocumentMetadataDto;
import com.code4ro.legalconsultation.document.metadata.model.dto.DocumentViewDto;
import com.code4ro.legalconsultation.document.metadata.model.persistence.DocumentMetadata;
import com.code4ro.legalconsultation.pdf.model.dto.PdfHandleDto;
import com.code4ro.legalconsultation.user.model.dto.UserDto;
import com.code4ro.legalconsultation.user.model.persistence.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public interface DocumentService {
    //return all metadata for documents
    Page<DocumentMetadata> fetchAll(Pageable pageable);

    //return document metadata for a single document
    DocumentMetadataDto fetchOne(final UUID id);

    //return the entire consolidated form of the document,
    //that contains the metadata and the breakdown into units
    DocumentConsolidatedDto fetchConsolidatedByMetadataId(final UUID id);

    /**
     * @param id of one of the document nodes contained in the requested document
     * @return the entire consolidated form of the document,
     * that contains the metadata and the breakdown into units
     */
    DocumentConsolidatedDto fetchConsolidatedByDocumentNodeId(final UUID id);

    //create a single document, including metadata and breakdown
    DocumentConsolidated create(final DocumentViewDto document);

    //update a single document
    DocumentConsolidated update(final UUID id,
                                final DocumentViewDto document);

    //delete a document based on id
    void deleteById(final UUID id);

    void assignUsers(final UUID id, final Set<UUID> userIds);

    List<UserDto> getAssignedUsers(final UUID id);

    PdfHandleDto addPdf(final UUID id, final String state, final MultipartFile file);

    byte[] export(final UUID id, final DocumentExportFormat type);

    List<UUID> getAllAssignedDocumentsNodeIds(User user);

    void addConsultationData(final UUID id, final DocumentConsultationDataDto consultationDataDto);

    DocumentConsultationDataDto getConsultationData(final UUID id);

}

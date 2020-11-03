package com.code4ro.legalconsultation.document.core.controller;

import com.code4ro.legalconsultation.core.exception.LegalValidationException;
import com.code4ro.legalconsultation.core.model.dto.PageDto;
import com.code4ro.legalconsultation.document.configuration.service.DocumentConfigurationService;
import com.code4ro.legalconsultation.document.consolidated.model.dto.DocumentConsolidatedDto;
import com.code4ro.legalconsultation.document.consolidated.model.dto.DocumentConsultationDataDto;
import com.code4ro.legalconsultation.document.consolidated.model.dto.DocumentUserAssignmentDto;
import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.document.core.service.DocumentService;
import com.code4ro.legalconsultation.document.export.model.DocumentExportFormat;
import com.code4ro.legalconsultation.document.metadata.model.dto.DocumentMetadataDto;
import com.code4ro.legalconsultation.document.metadata.model.dto.DocumentViewDto;
import com.code4ro.legalconsultation.document.metadata.model.persistence.DocumentMetadata;
import com.code4ro.legalconsultation.pdf.model.dto.PdfHandleDto;
import com.code4ro.legalconsultation.user.model.dto.UserDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentConfigurationService documentConfigurationService;

    @ApiOperation(value = "Return document metadata for all documents in the platform",
            response = PageDto.class,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("")
    public ResponseEntity<PageDto<DocumentMetadata>> getAllDocuments(@ApiParam("Page object information being requested") Pageable pageable) {
        Page<DocumentMetadata> documents = documentService.fetchAll(pageable);

        return ResponseEntity.ok(new PageDto<>(documents));
    }

    @ApiOperation(value = "Return document metadata for a single document in the platform based on id",
            response = DocumentMetadataDto.class,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/{id}")
    public ResponseEntity<DocumentMetadataDto> getDocumentMetadataById(@ApiParam("Id of the document object being requested") @PathVariable UUID id) {
        return ResponseEntity.ok(documentService.fetchOne(id));
    }

    @ApiOperation(value = "Return metadata and content for a single document in the platform based on metadata id",
            response = DocumentConsolidatedDto.class,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/{id}/consolidated")
    public ResponseEntity<DocumentConsolidatedDto> getDocumentConsolidatedById(@ApiParam("Id of the document metadata object being requested") @PathVariable UUID id) {
        return ResponseEntity.ok(documentService.fetchConsolidatedByMetadataId(id));
    }

    @GetMapping("/{id}/node")
    public ResponseEntity<DocumentConsolidatedDto> getDocumentConsolidatedByDocumentNodeId(
            @ApiParam("Id of on of the nodes included in the document being requested") @PathVariable UUID id) {
        return ResponseEntity.ok(documentService.fetchConsolidatedByDocumentNodeId(id));
    }

    @ApiOperation(value = "Delete metadata and contents for a single document in the platform based on id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@ApiParam("Id of the document object being deleted") @PathVariable UUID id) {
        documentService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Create a new document in the platform",
            response = UUID.class,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("")
    public ResponseEntity<UUID> createDocument(
            @Valid @RequestBody DocumentViewDto documentViewDto) {
        // TODO: decide the configuration for each document will be provided,
        // currently the service will initialise it with default true values
        DocumentConsolidated consolidated = documentService.create(documentViewDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(consolidated.getDocumentMetadata().getId());
    }

    @ApiOperation(value = "Modify a saved document in the platform",
            response = UUID.class,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PutMapping("/{id}")
    public ResponseEntity<UUID> modifyDocument(@ApiParam(value = "Id of the document being modified") @PathVariable("id") UUID id,
                                               @Valid @RequestBody DocumentViewDto documentViewDto) {
        DocumentConsolidated consolidated = documentService.update(id, documentViewDto);
        return ResponseEntity.ok(consolidated.getId());
    }

    @ApiOperation("Assign users to a document")
    @PostMapping("/{id}/users")
    public ResponseEntity<Void> assignUsers(
            @ApiParam(value = "Id of the document being modified") @PathVariable("id") UUID id,
            @Valid @RequestBody DocumentUserAssignmentDto documentUserAssignmentDto) {
        documentService.assignUsers(id, documentUserAssignmentDto.getUserIds());
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Get assigned users of a document")
    @GetMapping("{id}/users")
    public ResponseEntity<List<UserDto>> getAssignedUsers(
            @ApiParam(value = "Id of the document") @PathVariable("id") UUID id) {
        final List<UserDto> assignedUsers = documentService.getAssignedUsers(id);
        return ResponseEntity.ok(assignedUsers);
    }

    @ApiOperation(value = "Upload a pdf to this document",
            consumes = MediaType.APPLICATION_PDF_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            response = PdfHandleDto.class)
    @PostMapping("/{id}/pdf")
    public ResponseEntity<PdfHandleDto> uploadPdf(@ApiParam(value = "Id of the document") @PathVariable UUID id,
                                                  @ApiParam(value = "State of the pdf document") @RequestParam String state,
                                                  @ApiParam(value = "The pdf document") @RequestBody MultipartFile file) {
        try {
            PdfHandleDto pdfHandleDto = documentService.addPdf(id, state, file);

            return ResponseEntity.ok(pdfHandleDto);
        } catch(LegalValidationException ex){
            if(ex.getI18nKey().equals("document.pdf.upload.failed"))
                return ResponseEntity
                        .ok()
                        .build();

            throw ex;
        }
    }

    @ApiOperation(value = "Generate a PDF file from this document",
            consumes = MediaType.APPLICATION_PDF_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/{id}/export")
    public ResponseEntity<ByteArrayResource> generate(@PathVariable UUID id,
                                                      @RequestParam("type") DocumentExportFormat type) {
        final byte[] pdfContent = documentService.export(id, type);
        return ResponseEntity.ok()
                .contentLength(pdfContent.length)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new ByteArrayResource(pdfContent));
    }

    @ApiOperation("Add consultation data to a document")
    @PostMapping("/{metadataId}/consultation")
    public ResponseEntity<Void> addConsultationData(
            @ApiParam(value = "Id of the document metadata of the document being modified") @PathVariable("metadataId")
                    UUID metadataId,
            @Valid @RequestBody DocumentConsultationDataDto documentConsultationDataDto) {
        documentConfigurationService.addConsultationData(metadataId, documentConsultationDataDto);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Get consultation data of a document")
    @GetMapping("{metadataId}/consultation")
    public ResponseEntity<DocumentConsultationDataDto> getConsultationData(
            @ApiParam(value = "Id of the metadata of the document") @PathVariable("metadataId") UUID id) {
        final DocumentConsultationDataDto consultationData = documentConfigurationService.getConsultationData(id);
        return ResponseEntity.ok(consultationData);
    }
}

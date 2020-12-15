package com.code4ro.legalconsultation.document.core.service;

import com.code4ro.legalconsultation.comment.service.CommentService;
import com.code4ro.legalconsultation.document.consolidated.mapper.DocumentConsolidatedMapper;
import com.code4ro.legalconsultation.document.consolidated.service.DocumentConsolidatedService;
import com.code4ro.legalconsultation.core.factory.RandomObjectFiller;
import com.code4ro.legalconsultation.document.consolidated.model.dto.DocumentConsolidatedDto;
import com.code4ro.legalconsultation.document.metadata.model.dto.DocumentMetadataDto;
import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.document.metadata.model.persistence.DocumentMetadata;
import com.code4ro.legalconsultation.document.export.model.DocumentExportFormat;
import com.code4ro.legalconsultation.document.node.model.persistence.DocumentNode;
import com.code4ro.legalconsultation.pdf.model.persistence.PdfHandle;
import com.code4ro.legalconsultation.mail.service.MailApi;
import com.code4ro.legalconsultation.document.export.service.DocumentExporterFactory;
import com.code4ro.legalconsultation.document.export.service.impl.PDFExporter;
import com.code4ro.legalconsultation.document.metadata.service.DocumentMetadataService;
import com.code4ro.legalconsultation.document.core.service.impl.DocumentServiceImpl;
import com.code4ro.legalconsultation.pdf.service.impl.PDFServiceImpl;
import com.code4ro.legalconsultation.user.model.persistence.User;
import com.code4ro.legalconsultation.user.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceTest {

    @Mock
    private DocumentConsolidatedService documentConsolidatedService;
    @Mock
    private UserService userService;
    @Mock
    private DocumentMetadataService documentMetadataService;
    @Mock
    private MailApi mailService;
    private CommentService commentService;
    @Mock
    private DocumentConsolidatedMapper documentConsolidatedMapper;
    @Mock
    private PDFServiceImpl pdfService;
    @Mock
    private DocumentExporterFactory documentExporterFactory;
    @Mock
    private PDFExporter pdfExporter;

    @Captor
    private ArgumentCaptor<DocumentConsolidated> documentConsolidatedArgumentCaptor;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Test
    public void getDocument() {
        final UUID uuid = UUID.randomUUID();
        when(documentMetadataService.fetchOne(any(UUID.class))).thenReturn(new DocumentMetadataDto());

        documentService.fetchOne(uuid);

        verify(documentMetadataService).fetchOne(uuid);
    }

    @Test
    public void getDocumentConsolidated() {
        final UUID uuid = UUID.randomUUID();
        final DocumentConsolidated document = new DocumentConsolidated();
        final DocumentNode documentNode = new DocumentNode();
        documentNode.setId(UUID.randomUUID());
        document.setDocumentNode(documentNode);

        when(documentConsolidatedService.getByDocumentMetadataId(any(UUID.class))).thenReturn(document);
        when(documentConsolidatedMapper.map(any())).thenReturn(new DocumentConsolidatedDto());

        documentService.fetchConsolidatedDtoByMetadataId(uuid);

        verify(documentConsolidatedService).getByDocumentMetadataId(uuid);
    }

    @Test
    public void getAllDocuments() {
        documentService.fetchAll(Pageable.unpaged());

        verify(documentMetadataService).fetchAll(Pageable.unpaged());
    }

    @Test
    public void deleteDocument() {
        final UUID uuid = UUID.randomUUID();

        documentService.deleteById(uuid);

        verify(documentConsolidatedService).deleteById(uuid);
    }

    @Test
    public void assignUsers() {
        final DocumentMetadata metadata = RandomObjectFiller.createAndFillWithBaseEntity(DocumentMetadata.class);
        final DocumentConsolidated documentConsolidated = new DocumentConsolidated();
        documentConsolidated.setId(UUID.randomUUID());
        documentConsolidated.setDocumentMetadata(metadata);

        final User user1 = RandomObjectFiller.createAndFillWithBaseEntity(User.class);
        final User user2 = RandomObjectFiller.createAndFillWithBaseEntity(User.class);
        final User user3 = RandomObjectFiller.createAndFillWithBaseEntity(User.class);
        final List<User> assignedUsers = Arrays.asList(user1, user2, user3);
        final Set<UUID> assignedUsersIds = assignedUsers.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        when(documentConsolidatedService.getByDocumentMetadataId(documentConsolidated.getDocumentMetadata().getId()))
                .thenReturn(documentConsolidated);
        when(userService.findByIds(assignedUsersIds)).thenReturn(assignedUsers);

        documentService.assignUsers(documentConsolidated.getDocumentMetadata().getId(), assignedUsersIds);

        verify(mailService).sendDocumentAssignedEmail(eq(documentConsolidated.getDocumentMetadata()), eq(assignedUsers));
        verify(documentConsolidatedService).saveOne(documentConsolidatedArgumentCaptor.capture());
        final DocumentConsolidated capturedDocument = documentConsolidatedArgumentCaptor.getValue();
        assertThat(capturedDocument.getAssignedUsers()).hasSize(3);
        assertThat(capturedDocument.getAssignedUsers()).containsAll(assignedUsers);
    }

    @Test
    public void addPdf() {
        final UUID documentId = UUID.randomUUID();

        when(documentConsolidatedService.getEntity(any(UUID.class))).thenAnswer((Answer<DocumentConsolidated>) invocationOnMock -> {
            DocumentConsolidated documentConsolidated = new DocumentConsolidated();
            documentConsolidated.setId(documentId);
            return documentConsolidated;
        });
        final DocumentConsolidated documentConsolidated = documentConsolidatedService.getEntity(documentId);

        assertThat(documentConsolidated.getId()).isEqualTo(documentId);

        when(pdfService.createPdf(any(DocumentConsolidated.class), any(), any())).thenAnswer((Answer<PdfHandle>) invocationOnMock -> {
            PdfHandle pdfHandle = new PdfHandle();
            pdfHandle.setOwner(documentConsolidated);
            return pdfHandle;
        });

        final PdfHandle pdfHandle = pdfService.createPdf(documentConsolidated, null, null);

        assertThat(documentConsolidated).isEqualTo(pdfHandle.getOwner());
    }

    @Test
    public void exportDocumentToPdf() {
        final UUID id = UUID.randomUUID();
        final byte[] pdfContent = new byte[]{1, 2, 3};

        when(documentExporterFactory.getExporter(DocumentExportFormat.PDF)).thenReturn(pdfExporter);
        when(pdfExporter.export(any())).thenReturn(pdfContent);

        final byte[] result = documentService.export(id, DocumentExportFormat.PDF);

        verify(documentExporterFactory).getExporter(DocumentExportFormat.PDF);
        verify(documentConsolidatedService).getEntity(id);

        assertThat(result)
                .isNotEmpty()
                .hasSameSizeAs(pdfContent)
                .isEqualTo(pdfContent);
    }
}

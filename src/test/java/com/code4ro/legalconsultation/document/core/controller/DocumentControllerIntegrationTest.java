package com.code4ro.legalconsultation.document.core.controller;

import com.amazonaws.util.json.Jackson;
import com.code4ro.legalconsultation.comment.factory.CommentFactory;
import com.code4ro.legalconsultation.comment.model.dto.CommentDetailDto;
import com.code4ro.legalconsultation.comment.model.persistence.CommentStatus;
import com.code4ro.legalconsultation.comment.service.CommentService;
import com.code4ro.legalconsultation.core.controller.AbstractControllerIntegrationTest;
import com.code4ro.legalconsultation.core.factory.RandomObjectFiller;
import com.code4ro.legalconsultation.document.configuration.model.persistence.DocumentConfiguration;
import com.code4ro.legalconsultation.document.consolidated.model.dto.DocumentConsultationDataDto;
import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.document.consolidated.repository.DocumentConsolidatedRepository;
import com.code4ro.legalconsultation.document.metadata.model.dto.DocumentViewDto;
import com.code4ro.legalconsultation.document.metadata.model.persistence.DocumentMetadata;
import com.code4ro.legalconsultation.document.metadata.repository.DocumentMetadataRepository;
import com.code4ro.legalconsultation.document.node.factory.DocumentNodeFactory;
import com.code4ro.legalconsultation.document.node.model.persistence.DocumentNode;
import com.code4ro.legalconsultation.document.node.repository.DocumentNodeRepository;
import com.code4ro.legalconsultation.pdf.factory.PdfFileFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class DocumentControllerIntegrationTest extends AbstractControllerIntegrationTest {

    @Value("${storage.filesystem.directory}")
    private String customStoreDirPath;

    @Autowired
    private DocumentMetadataRepository documentMetadataRepository;
    @Autowired
    private DocumentConsolidatedRepository documentConsolidatedRepository;
    @Autowired
    private DocumentNodeRepository documentNodeRepository;
    @Autowired
    private DocumentNodeFactory documentNodeFactory;
    @Autowired
    private CommentFactory commentFactory;
    @Autowired
    private CommentService commentService;

    @Test
    @WithMockUser
    @Transactional
    public void saveDocument() throws Exception {
        final File file = PdfFileFactory.getAsFiles(getClass().getClassLoader()).get(0);
        final DocumentViewDto randomView = RandomObjectFiller.createAndFill(DocumentViewDto.class);
        randomView.setFilePath(file.getPath());

        mvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Jackson.toJsonString(randomView))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertThat(documentMetadataRepository.count()).isEqualTo(1);
        assertThat(documentConsolidatedRepository.count()).isEqualTo(1);
        assertThat(documentNodeRepository.count()).isEqualTo(14);

        final String soredFilePath = documentMetadataRepository.findAll().get(0).getFilePath();
        assertThatDocumentIsStored(soredFilePath);

        final List<DocumentConsolidated> documents = documentConsolidatedRepository.findAll();
        final DocumentConsolidated document = documents.iterator().next();
        assertThatDocumentNodeTreeIsGeneratedCorrectly(document.getDocumentNode());
    }

    @Test
    @WithMockUser
    @Transactional
    public void saveDocumentDuplicatedNumber() throws Exception {
        final File file = PdfFileFactory.getAsFiles(getClass().getClassLoader()).get(0);
        final DocumentViewDto firstDocument = RandomObjectFiller.createAndFill(DocumentViewDto.class);
        firstDocument.setFilePath(file.getPath());

        mvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Jackson.toJsonString(firstDocument))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        final DocumentViewDto secondDocument = RandomObjectFiller.createAndFill(DocumentViewDto.class);
        secondDocument.setDocumentNumber(firstDocument.getDocumentNumber());

        mvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Jackson.toJsonString(secondDocument))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    @WithMockUser
    @Transactional
    public void saveDocumentDuplicatedFilePath() throws Exception {
        final File file = PdfFileFactory.getAsFiles(getClass().getClassLoader()).get(0);
        final DocumentViewDto firstDocument = RandomObjectFiller.createAndFill(DocumentViewDto.class);
        firstDocument.setFilePath(file.getPath());

        mvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Jackson.toJsonString(firstDocument))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        final DocumentViewDto secondDocument = RandomObjectFiller.createAndFill(DocumentViewDto.class);
        secondDocument.setFilePath(file.getPath());

        mvc.perform(post("/api/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Jackson.toJsonString(secondDocument))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    private void assertThatDocumentNodeTreeIsGeneratedCorrectly(final DocumentNode document) {
        final DocumentNode expectedDocument = documentNodeFactory.createDocument(null, "Document title  on multiple lines", "Sample document introduction that can be on one line or on multiplelines");
        assertThat(document.getChildren()).hasSize(2);
        assertDocumentNodeContent(expectedDocument, document);

        final DocumentNode expectedChapter1 = documentNodeFactory.createChapter("I", "Chapter title on multiple lines  ", null);
        expectedChapter1.setParent(document);
        final DocumentNode chapter1 = document.getChildren().get(0);
        assertThat(chapter1.getChildren()).hasSize(2);
        assertDocumentNodeContent(expectedChapter1, chapter1);

        final DocumentNode expectedChapter2 = documentNodeFactory.createChapter("II", "Chapter title on single line", null);
        expectedChapter2.setParent(document);
        final DocumentNode chapter2 = document.getChildren().get(1);
        assertThat(chapter1.getChildren()).hasSize(2);
        assertDocumentNodeContent(expectedChapter2, chapter2);

        final DocumentNode expectedArticle4 = documentNodeFactory.createArticle("4", null, "Article without children");
        expectedArticle4.setParent(chapter2);
        final DocumentNode article4 = chapter2.getChildren().get(0);
        assertThat(article4.getChildren()).isEmpty();
        assertDocumentNodeContent(expectedArticle4, article4);

        final DocumentNode expectedSection1 = documentNodeFactory.createSection("1", "Section with title", null);
        expectedSection1.setParent(chapter1);
        final DocumentNode section1 = chapter1.getChildren().get(0);
        assertThat(section1.getChildren()).hasSize(2);
        assertDocumentNodeContent(expectedSection1, section1);

        final DocumentNode expectedSection2 = documentNodeFactory.createSection("2", "", "");
        expectedSection2.setParent(chapter1);
        final DocumentNode section2 = chapter1.getChildren().get(1);
        assertThat(section1.getChildren()).hasSize(2);
        assertDocumentNodeContent(expectedSection2, section2);

        final DocumentNode expectedArticle1 = documentNodeFactory.createArticle("1", null, null);
        expectedArticle1.setParent(section1);
        final DocumentNode article1 = section1.getChildren().get(0);
        assertThat(article1.getChildren()).hasSize(1);
        assertDocumentNodeContent(expectedArticle1, article1);

        final DocumentNode expectedArticle2 = documentNodeFactory.createArticle("2", "Article with title on one line", null);
        expectedArticle2.setParent(section1);
        final DocumentNode article2 = section1.getChildren().get(1);
        assertThat(article2.getChildren()).hasSize(1);
        assertDocumentNodeContent(expectedArticle2, article2);

        final DocumentNode expectedArticle3 = documentNodeFactory.createArticle("3", "Article with title on multiple lines", null);
        expectedArticle3.setParent(section2);
        final DocumentNode article3 = section2.getChildren().get(0);
        assertThat(article2.getChildren()).hasSize(1);
        assertDocumentNodeContent(expectedArticle3, article3);

        final DocumentNode expectedParagraph11 = documentNodeFactory.createParagraph("1", null, "Paragraph content on a single line");
        expectedParagraph11.setParent(article1);
        final DocumentNode paragraph11 = article1.getChildren().get(0);
        assertThat(paragraph11.getChildren()).isEmpty();
        assertDocumentNodeContent(expectedParagraph11, paragraph11);

        final DocumentNode expectedParagraph21 = documentNodeFactory.createParagraph("1", null, "Paragraph content on multiple lines");
        expectedParagraph21.setParent(article2);
        final DocumentNode paragraph21 = article2.getChildren().get(0);
        assertThat(paragraph21.getChildren()).isEmpty();
        assertDocumentNodeContent(expectedParagraph21, paragraph21);

        final DocumentNode expectedParagraph35 = documentNodeFactory.createParagraph("5", null, "Paragraph with multiple letters");
        expectedParagraph35.setParent(article3);
        final DocumentNode paragraph35 = article3.getChildren().get(0);
        assertThat(paragraph35.getChildren()).hasSize(2);
        assertDocumentNodeContent(expectedParagraph35, paragraph35);

        final DocumentNode expectedAlignmentA = documentNodeFactory.createAlignment("a", null, "Sample letter content 1");
        expectedAlignmentA.setParent(paragraph35);
        final DocumentNode alignmentA = paragraph35.getChildren().get(0);
        assertThat(alignmentA.getChildren()).isEmpty();
        assertDocumentNodeContent(expectedAlignmentA, alignmentA);

        final DocumentNode expectedAlignmentB = documentNodeFactory.createAlignment("b", null, "Sample letter content 2");
        expectedAlignmentB.setParent(paragraph35);
        final DocumentNode alignmentB = paragraph35.getChildren().get(1);
        assertThat(alignmentA.getChildren()).isEmpty();
        assertDocumentNodeContent(expectedAlignmentB, alignmentB);
    }

    private void assertDocumentNodeContent(final DocumentNode expected, final DocumentNode actual) {
        assertThat(expected.getIdentifier()).isEqualToIgnoringWhitespace(actual.getIdentifier());
        assertThat(expected.getTitle()).isEqualToIgnoringWhitespace(actual.getTitle());
        assertThat(expected.getContent()).isEqualToIgnoringWhitespace(actual.getContent());
        assertThat(expected.getDocumentNodeType()).isEqualByComparingTo(actual.getDocumentNodeType());
        assertThat(expected.getParent()).isEqualTo(actual.getParent());
    }

    private void assertThatDocumentIsStored(String soredFilePath) {
        assertThat(Files.exists(Paths.get(soredFilePath))).isTrue();
        // remove the store directory
        final File storeDir = new File(customStoreDirPath);
        FileSystemUtils.deleteRecursively(storeDir);
        storeDir.deleteOnExit();
    }

    @Test
    @WithMockUser
    @Transactional
    public void getDocument() throws Exception {
        DocumentConsolidated consolidated = saveSingleConsolidated();

        mvc.perform(get(endpoint("/api/documents/", consolidated.getDocumentMetadata().getId()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(consolidated.getDocumentMetadata().getId().toString()))
                .andExpect(status().isOk());

        mvc.perform(get(endpoint("/api/documents/", consolidated.getDocumentMetadata().getId(), "/consolidated"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(consolidated.getId().toString()))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(documentMetadataRepository.count()).isEqualTo(1);
        assertThat(documentConsolidatedRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser
    @Transactional
    public void getDocumentCommentsNumber() throws Exception {
        persistMockedUser();
        DocumentConsolidated consolidated = saveSingleConsolidated();

        CommentDetailDto comment1 = commentFactory.save(consolidated.getDocumentNode().getId());
        CommentDetailDto comment2 = commentFactory.save(consolidated.getDocumentNode().getId());

        final CommentDetailDto reply1 = commentFactory.saveReply(comment1.getId());
        final CommentDetailDto reply2 = commentFactory.saveReply(comment1.getId());
        final CommentDetailDto reply3 = commentFactory.saveReply(comment2.getId());

        commentService.setStatus(comment1.getId(), CommentStatus.APPROVED);
        commentService.setStatus(comment2.getId(), CommentStatus.APPROVED);
        commentService.setStatus(reply1.getId(), CommentStatus.APPROVED);
        commentService.setStatus(reply2.getId(), CommentStatus.APPROVED);
        commentService.setStatus(reply3.getId(), CommentStatus.APPROVED);

        mvc.perform(get(endpoint("/api/documents/", consolidated.getDocumentMetadata().getId(), "/consolidated"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.documentNode.numberOfComments").value("5"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @WithMockUser
    @Transactional
    public void listDocuments() throws Exception {
        List<DocumentConsolidated> documentsConsolidated = new ArrayList<>();

        documentsConsolidated.add(saveSingleConsolidated());
        documentsConsolidated.add(saveSingleConsolidated());

        mvc.perform(get("/api/documents/")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(documentsConsolidated.get(0).getDocumentMetadata().getId().toString()))
                .andExpect(jsonPath("$.content[1].id").value(documentsConsolidated.get(1).getDocumentMetadata().getId().toString()))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(status().isOk());

        assertThat(documentMetadataRepository.count()).isEqualTo(2);
        assertThat(documentConsolidatedRepository.count()).isEqualTo(2);
    }

    @Test
    @WithMockUser
    @Transactional
    public void testGetDocumentsPagination() throws Exception {
        List<DocumentConsolidated> documentsConsolidated = new ArrayList<>();

        documentsConsolidated.add(saveSingleConsolidated());
        documentsConsolidated.add(saveSingleConsolidated());
        documentsConsolidated.add(saveSingleConsolidated());

        mvc.perform(get("/api/documents/")
                .accept(MediaType.APPLICATION_JSON)
                .param("page", "0")
                .param("size", "2"))
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(documentsConsolidated.get(0).getDocumentMetadata().getId().toString()))
                .andExpect(jsonPath("$.content[1].id").value(documentsConsolidated.get(1).getDocumentMetadata().getId().toString()))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(status().isOk());

        mvc.perform(get("/api/documents/")
                .accept(MediaType.APPLICATION_JSON)
                .param("page", "1")
                .param("size", "2"))
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(documentsConsolidated.get(2).getDocumentMetadata().getId().toString()))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.pageable.pageNumber").value(1))
                .andExpect(status().isOk());

        assertThat(documentMetadataRepository.count()).isEqualTo(3);
        assertThat(documentConsolidatedRepository.count()).isEqualTo(3);
    }

    @Test
    @WithMockUser
    @Transactional
    public void testGetDocumentsSorting() throws Exception {
        List<DocumentConsolidated> documentsConsolidated = new ArrayList<>();

        documentsConsolidated.add(saveSingleConsolidated());
        documentsConsolidated.add(saveSingleConsolidated());

        documentsConsolidated.sort(Comparator.comparing(d -> d.getDocumentMetadata().getDateOfDevelopment()));

        mvc.perform(get("/api/documents/")
                .accept(MediaType.APPLICATION_JSON)
                .param("sort", "dateOfDevelopment"))
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(documentsConsolidated.get(0).getDocumentMetadata().getId().toString()))
                .andExpect(jsonPath("$.content[1].id").value(documentsConsolidated.get(1).getDocumentMetadata().getId().toString()))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(status().isOk());

        documentsConsolidated.sort(Comparator.comparing(d -> d.getDocumentMetadata().getDocumentInitializer()));

        mvc.perform(get("/api/documents/")
                .accept(MediaType.APPLICATION_JSON)
                .param("sort", "documentInitializer"))
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(documentsConsolidated.get(0).getDocumentMetadata().getId().toString()))
                .andExpect(jsonPath("$.content[1].id").value(documentsConsolidated.get(1).getDocumentMetadata().getId().toString()))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(status().isOk());

        assertThat(documentMetadataRepository.count()).isEqualTo(2);
        assertThat(documentConsolidatedRepository.count()).isEqualTo(2);
    }

    @Test
    @WithMockUser
    @Transactional
    public void testGetDocumentsPaginationForNonExistingPage() throws Exception {
        saveSingleConsolidated();

        mvc.perform(get("/api/documents/")
                .accept(MediaType.APPLICATION_JSON)
                .param("page", "10"))
                .andExpect(jsonPath("$.content.size()").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @Transactional
    public void testGetDocumentsSortingByNonExistentField() throws Exception {
        mvc.perform(get("/api/documents/")
                .accept(MediaType.APPLICATION_JSON)
                .param("sort", "nonExistentField"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    @Transactional
    public void testGetDocumentsPaginationAndSorting() throws Exception {
        List<DocumentConsolidated> documentsConsolidated = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            documentsConsolidated.add(saveSingleConsolidated());
        }

        documentsConsolidated.sort(Comparator.comparing(d -> d.getDocumentMetadata().getDocumentInitializer()));

        mvc.perform(get("/api/documents/")
                .accept(MediaType.APPLICATION_JSON)
                .param("sort", "documentInitializer")
                .param("page", "2")
                .param("size", "3"))
                .andExpect(jsonPath("$.content.size()").value(3))
                .andExpect(jsonPath("$.content[0].id").value(documentsConsolidated.get(6).getDocumentMetadata().getId().toString()))
                .andExpect(jsonPath("$.content[1].id").value(documentsConsolidated.get(7).getDocumentMetadata().getId().toString()))
                .andExpect(jsonPath("$.content[2].id").value(documentsConsolidated.get(8).getDocumentMetadata().getId().toString()))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @Transactional
    public void deleteDocument() throws Exception {
        DocumentConsolidated consolidated = saveSingleConsolidated();

        mvc.perform(delete("/api/documents/" + consolidated.getId().toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(documentMetadataRepository.count()).isZero();
        assertThat(documentConsolidatedRepository.count()).isZero();
    }

    @Test
    @WithMockUser
    @Transactional
    public void retrieveDocumentByInnerNodeId() throws Exception {
        DocumentConsolidated consolidated = saveSingleConsolidated();
        DocumentNode node = consolidated.getDocumentNode();

        mvc.perform(get("/api/documents/" + node.getId() + "/node")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(consolidated.getId().toString()))
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser
    public void testGetDocumentNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();

        mvc.perform(get("/api/documents/" + uuid.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testGetDocumentConsolidatedNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();

        mvc.perform(get("/api/documents/" + uuid.toString() + "/consolidated"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void deleteDocumentNotFound() throws Exception {
        UUID uuid = UUID.randomUUID();

        mvc.perform(delete("/api/documents/" + uuid.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @Transactional
    public void addPdf() throws Exception {
        DocumentConsolidated consolidated = saveSingleConsolidated();

        final String state = "abcdef";
        final MockMultipartFile randomFile = PdfFileFactory
                .getAsMultipart(getClass().getClassLoader());

        mvc.perform(multipart("/api/documents/" + consolidated.getId().toString() + "/pdf")
                .file(randomFile)
                .contentType(MediaType.APPLICATION_PDF)
                .param("state", state))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @Transactional
    public void testExportDocument() throws Exception {
        final DocumentConsolidated document = saveSingleConsolidated();

        mvc.perform(get("/api/documents/" + document.getId().toString() + "/export")
                .param("type", "PDF"))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @Transactional
    public void addConsultationData() throws Exception {
        DocumentConsolidated consolidated = saveSingleConsolidated();

        Date startDate = new Date();
        Date consultationDeadline = new Date();

        final DocumentConsultationDataDto documentConsultationDataDto = new DocumentConsultationDataDto(
                startDate, consultationDeadline, true);


        mvc.perform(
                multipart("/api/documents/" + consolidated.getDocumentMetadata().getId().toString() + "/consultation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Jackson.toJsonString(documentConsultationDataDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/api/documents/" + consolidated.getDocumentMetadata().getId().toString() + "/consultation")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").isNotEmpty())
                .andExpect(jsonPath("$.consultationDeadline").isNotEmpty())
                .andExpect(jsonPath("$.excludedFromConsultation").value(true));
    }


    private DocumentConsolidated saveSingleConsolidated() {
        final DocumentNode documentNode = documentNodeFactory.save();
        final DocumentMetadata documentMetadata = RandomObjectFiller.createAndFill(DocumentMetadata.class);
        final DocumentConfiguration documentConfiguration =
                RandomObjectFiller.createAndFill(DocumentConfiguration.class);
        DocumentConsolidated consolidated =
                new DocumentConsolidated(documentMetadata, documentNode, documentConfiguration);
        return documentConsolidatedRepository.save(consolidated);
    }
}

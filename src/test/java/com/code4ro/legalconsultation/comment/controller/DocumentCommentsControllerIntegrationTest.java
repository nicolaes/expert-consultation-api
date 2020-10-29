package com.code4ro.legalconsultation.comment.controller;

import com.code4ro.legalconsultation.authentication.service.ApplicationUserService;
import com.code4ro.legalconsultation.comment.model.dto.CommentDetailDto;
import com.code4ro.legalconsultation.comment.model.dto.CommentDto;
import com.code4ro.legalconsultation.comment.model.persistence.Comment;
import com.code4ro.legalconsultation.comment.service.CommentService;
import com.code4ro.legalconsultation.core.controller.AbstractControllerIntegrationTest;
import com.code4ro.legalconsultation.comment.factory.CommentFactory;
import com.code4ro.legalconsultation.document.node.factory.DocumentNodeFactory;
import com.code4ro.legalconsultation.authentication.model.persistence.ApplicationUser;
import com.code4ro.legalconsultation.document.node.model.persistence.DocumentNode;
import com.code4ro.legalconsultation.comment.repository.CommentRepository;
import com.code4ro.legalconsultation.security.service.CurrentUserService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static com.code4ro.legalconsultation.comment.model.persistence.CommentStatus.APPROVED;
import static com.code4ro.legalconsultation.comment.model.persistence.CommentStatus.REJECTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnableJpaAuditing
public class DocumentCommentsControllerIntegrationTest extends AbstractControllerIntegrationTest {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CommentService commentService;
    @Autowired
    private CommentFactory commentFactory;
    @Autowired
    private DocumentNodeFactory documentNodeFactory;
    @Autowired
    private CurrentUserService currentUserService;

    @Before
    public void before() {
        persistMockedUser();
    }

    @Test
    @WithMockUser
    @Transactional
    public void create() throws Exception {
        final DocumentNode node = documentNodeFactory.save();
        final CommentDto commentDto = commentFactory.create();

        mvc.perform(post(endpoint("/api/documentnodes/", node.getId(), "/comments"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(status().isOk());

        assertThat(commentRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser
    @Transactional
    public void update() throws Exception {
        final DocumentNode node = documentNodeFactory.save();
        final CommentDto commentDto = commentFactory.create();
        final ApplicationUser currentUser = currentUserService.getCurrentUser();

        Comment comment = commentFactory.createEntity();
        comment.setDocumentNode(node);
        comment.setOwner(currentUser);
        comment.setLastEditDateTime(new Date());
        comment = commentRepository.save(comment);

        final String newText = "new text";
        commentDto.setText(newText);

        mvc.perform(put(endpoint("/api/documentnodes/", node.getId(), "/comments/", comment.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value(newText))
                .andExpect(status().isOk());

        assertThat(commentRepository.getOne(comment.getId()).getText()).isEqualTo(newText);
    }

    @Test
    @WithMockUser
    @Transactional
    public void deleteComment() throws Exception {
        final DocumentNode node = documentNodeFactory.save();
        final CommentDto commentDto = commentFactory.create();
        final ApplicationUser currentUser = currentUserService.getCurrentUser();

        Comment comment = commentFactory.createEntity();
        comment.setDocumentNode(node);
        comment.setOwner(currentUser);
        comment.setLastEditDateTime(new Date());
        comment = commentRepository.save(comment);

        mvc.perform(delete(endpoint("/api/documentnodes/", node.getId(), "/comments/", comment.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(commentRepository.count()).isEqualTo(0);
    }

    @Test
    @WithMockUser
    @Transactional
    public void findAll() throws Exception {
        final DocumentNode node = documentNodeFactory.save();
        final CommentDetailDto comment1 = commentService.create(node.getId(), commentFactory.create());
        final CommentDetailDto comment2 = commentService.create(node.getId(), commentFactory.create());
        final CommentDetailDto comment3 = commentService.create(node.getId(), commentFactory.create());

        commentService.setStatus(comment1.getId(), APPROVED);
        commentService.setStatus(comment2.getId(), APPROVED);
        commentService.setStatus(comment3.getId(), APPROVED);

        mvc.perform(get(endpoint("/api/documentnodes/", node.getId(), "/comments?page=0"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.pageable.pageSize").value(2))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(status().isOk());

        mvc.perform(get(endpoint("/api/documentnodes/", node.getId(), "/comments?page=1"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.pageable.pageSize").value(2))
                .andExpect(jsonPath("$.pageable.pageNumber").value(1))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @Transactional
    public void approve() throws Exception {
        final DocumentNode node = documentNodeFactory.save();
        CommentDetailDto comment = commentService.create(node.getId(), commentFactory.create());
        mvc.perform(get(endpoint("/api/comments/", comment.getId(), "/approve"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(APPROVED.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @Transactional
    public void reject() throws Exception {
        final DocumentNode node = documentNodeFactory.save();
        CommentDetailDto comment = commentService.create(node.getId(), commentFactory.create());
        mvc.perform(get(endpoint("/api/comments/", comment.getId(), "/reject"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(REJECTED.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @Transactional
    public void findAllReplies() throws Exception {
        DocumentNode node = documentNodeFactory.save();
        Comment comment = createComment(node);

        commentService.createReply(comment.getId(), commentFactory.create());
        commentService.createReply(comment.getId(), commentFactory.create());
        commentService.createReply(comment.getId(), commentFactory.create());

        mvc.perform(get(endpoint("/api/documentnodes/", node.getId(), "/comments", comment.getId(), "/replies?page=0"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(status().isOk());

        mvc.perform(get(endpoint("/api/documentnodes/", node.getId(), "/comments", comment.getId(), "/replies?page=1"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andExpect(jsonPath("$.content[0].text").isNotEmpty())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @Transactional
    public void createReply() throws Exception {
        DocumentNode node = documentNodeFactory.save();
        Comment comment = createComment(node);
        CommentDto commentDto = commentFactory.create();

        assertThat(commentRepository.count()).isEqualTo(1);

        mvc.perform(post(endpoint("/api/documentnodes/", node.getId(), "/comments/", comment.getId(), "/replies"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(status().isOk());

        assertThat(commentRepository.count()).isEqualTo(2);
    }

    private Comment createComment(DocumentNode node) {
        Comment comment = commentFactory.createEntity();
        comment.setDocumentNode(node);
        comment.setOwner(currentUserService.getCurrentUser());
        comment.setLastEditDateTime(new Date());
        return commentRepository.save(comment);
    }
}

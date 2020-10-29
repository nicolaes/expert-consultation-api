package com.code4ro.legalconsultation.comment.controller;

import com.code4ro.legalconsultation.authentication.model.dto.SignUpRequest;
import com.code4ro.legalconsultation.authentication.model.persistence.ApplicationUser;
import com.code4ro.legalconsultation.comment.factory.CommentFactory;
import com.code4ro.legalconsultation.comment.model.dto.CommentDetailDto;
import com.code4ro.legalconsultation.comment.model.persistence.CommentStatus;
import com.code4ro.legalconsultation.comment.service.CommentService;
import com.code4ro.legalconsultation.core.controller.AbstractControllerIntegrationTest;
import com.code4ro.legalconsultation.core.factory.RandomObjectFiller;
import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.document.core.service.DocumentService;
import com.code4ro.legalconsultation.document.node.factory.DocumentFactory;
import com.code4ro.legalconsultation.invitation.model.persistence.Invitation;
import com.code4ro.legalconsultation.invitation.model.persistence.InvitationStatus;
import com.code4ro.legalconsultation.user.factory.UserFactory;
import com.code4ro.legalconsultation.user.model.persistence.User;
import com.code4ro.legalconsultation.user.model.persistence.UserRole;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnableJpaAuditing
public class CommentsControllerTest extends AbstractControllerIntegrationTest {
    @Autowired
    private CommentService commentService;
    @Autowired
    private DocumentFactory documentFactory;
    @Autowired
    private CommentFactory commentFactory;
    @Autowired
    private UserFactory userFactory;
    @Autowired
    private DocumentService documentService;

    @Test
    @WithMockUser
    @Transactional
    public void findAllPendingWithAdminUser() throws Exception {
        persistCurrentUser(UserRole.ADMIN);

        final DocumentConsolidated document1 = documentFactory.create();
        final DocumentConsolidated document2 = documentFactory.create();
        final DocumentConsolidated document3 = documentFactory.create();

        commentService.create(document1.getDocumentNode().getId(), commentFactory.create());
        commentService.create(document2.getDocumentNode().getChildren().get(0).getId(), commentFactory.create());
        final CommentDetailDto comment3 = commentService
                .create(document3.getDocumentNode().getChildren().get(0).getId(), commentFactory.create());
        commentService.create(document3.getDocumentNode().getId(), commentFactory.create());
        commentService.setStatus(comment3.getId(), CommentStatus.APPROVED);

        mvc.perform(get(endpoint("/api/comments/pending?page=0&size=10"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @Transactional
    public void findAllPendingWithOwnerUser() throws Exception {
        persistCurrentUser(UserRole.OWNER);

        final DocumentConsolidated document1 = documentFactory.create();
        final DocumentConsolidated document2 = documentFactory.create();
        final DocumentConsolidated document3 = documentFactory.create();

        commentService.create(document1.getDocumentNode().getId(), commentFactory.create());
        commentService.create(document2.getDocumentNode().getChildren().get(0).getId(), commentFactory.create());
        final CommentDetailDto comment3 = commentService
                .create(document3.getDocumentNode().getChildren().get(0).getId(), commentFactory.create());
        commentService.create(document3.getDocumentNode().getId(), commentFactory.create());
        commentService.setStatus(comment3.getId(), CommentStatus.APPROVED);

        mvc.perform(get(endpoint("/api/comments/pending?page=0&size=10"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @Transactional
    public void findAllPendingWithRegularUser() throws Exception {
        final ApplicationUser applicationUser = persistCurrentUser(UserRole.CONTRIBUTOR);

        final DocumentConsolidated document1 = documentFactory.create();
        final DocumentConsolidated document2 = documentFactory.create();
        final DocumentConsolidated document3 = documentFactory.create();

        documentService.assignUsers(document1.getDocumentMetadata().getId(), Set.of(applicationUser.getUser().getId()));
        documentService.assignUsers(document3.getDocumentMetadata().getId(), Set.of(applicationUser.getUser().getId()));

        final CommentDetailDto comment1 = commentService.create(document1.getDocumentNode().getId(), commentFactory.create());
        final CommentDetailDto comment2 = commentService.create(document2.getDocumentNode().getChildren().get(0).getId(), commentFactory.create());
        final CommentDetailDto comment3 = commentService
                .create(document3.getDocumentNode().getChildren().get(0).getId(), commentFactory.create());
        final CommentDetailDto comment4 = commentService.create(document3.getDocumentNode().getId(), commentFactory.create());
        commentService.setStatus(comment3.getId(), CommentStatus.APPROVED);

        final MvcResult mvcResult = mvc.perform(get(endpoint("/api/comments/pending?page=0&size=10"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(status().isOk())
                .andReturn();

        final String contentAsString = mvcResult.getResponse().getContentAsString();

        assertThat(contentAsString).contains(comment1.getId().toString());
        assertThat(contentAsString).contains(comment4.getId().toString());
        assertThat(contentAsString).doesNotContain(comment3.getId().toString());
        assertThat(contentAsString).doesNotContain(comment2.getId().toString());
    }

    private ApplicationUser persistCurrentUser(final UserRole role) {
        final ApplicationUser applicationUser = userFactory.createApplicationUserWithRole(role);
        final User user = userRepository.save(applicationUser.getUser());
        final Invitation invitation = RandomObjectFiller.createAndFill(Invitation.class);
        invitation.setUser(user);
        invitation.setStatus(InvitationStatus.PENDING);
        invitationRepository.save(invitation);

        final SignUpRequest signUpRequest = RandomObjectFiller.createAndFill(SignUpRequest.class);
        signUpRequest.setUsername("user");
        signUpRequest.setPassword("password");
        signUpRequest.setInvitationCode(invitation.getCode());
        signUpRequest.setEmail(user.getEmail());
        applicationUserService.save(signUpRequest);

        applicationUser.setUser(user);
        return applicationUser;
    }
}

package com.code4ro.legalconsultation.comment.service;

import com.code4ro.legalconsultation.comment.mapper.CommentMapper;
import com.code4ro.legalconsultation.comment.model.dto.CommentDto;
import com.code4ro.legalconsultation.comment.model.persistence.Comment;
import com.code4ro.legalconsultation.comment.model.persistence.CommentStatus;
import com.code4ro.legalconsultation.comment.service.impl.CommentServiceImpl;
import com.code4ro.legalconsultation.core.exception.LegalValidationException;
import com.code4ro.legalconsultation.comment.factory.CommentFactory;
import com.code4ro.legalconsultation.document.core.service.DocumentService;
import com.code4ro.legalconsultation.document.node.factory.DocumentNodeFactory;
import com.code4ro.legalconsultation.core.factory.RandomObjectFiller;
import com.code4ro.legalconsultation.authentication.model.persistence.ApplicationUser;
import com.code4ro.legalconsultation.document.node.model.persistence.DocumentNode;
import com.code4ro.legalconsultation.comment.repository.CommentRepository;
import com.code4ro.legalconsultation.document.node.service.DocumentNodeService;
import com.code4ro.legalconsultation.security.service.CurrentUserService;
import com.code4ro.legalconsultation.user.factory.UserFactory;
import com.code4ro.legalconsultation.user.model.persistence.UserRole;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommentServiceTest {

    public static final CommentStatus NEW_STATUS = CommentStatus.APPROVED;
    private final DocumentNodeFactory documentNodeFactory = new DocumentNodeFactory();
    private final CommentFactory commentFactory = new CommentFactory();
    private final UserFactory userFactory = new UserFactory();

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapper mapperService;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private DocumentNodeService documentNodeService;
    @Mock
    private DocumentService documentService;
    @InjectMocks
    private CommentServiceImpl commentService;
    @Captor
    private ArgumentCaptor<Comment> commentArgumentCaptor;
    private ApplicationUser currentUser;

    @Before
    public void before() {
        currentUser = RandomObjectFiller.createAndFill(ApplicationUser.class);
        currentUser.getUser().setRole(UserRole.ADMIN);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    public void create() {
        final UUID id = UUID.randomUUID();
        final CommentDto commentDto = RandomObjectFiller.createAndFill(CommentDto.class);
        final DocumentNode documentNode = documentNodeFactory.create();
        final Comment comment = new Comment();
        when(mapperService.map(commentDto)).thenReturn(comment);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(documentNodeService.findById(any())).thenReturn(documentNode);

        commentService.create(id, commentDto);

        verify(commentRepository).save(comment);
        assertThat(comment.getOwner()).isEqualTo(currentUser);
        assertThat(comment.getDocumentNode()).isEqualTo(documentNode);
    }

    @Test
    public void update() {
        final UUID id = UUID.randomUUID();
        final CommentDto commentDto = RandomObjectFiller.createAndFill(CommentDto.class);
        final Comment comment = new Comment();
        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));

        commentService.update(UUID.randomUUID(), id, commentDto);

        verify(commentRepository).save(commentArgumentCaptor.capture());
        assertThat(commentArgumentCaptor.getValue().getText()).isEqualTo(commentDto.getText());
    }

    @Test(expected = LegalValidationException.class)
    public void updateUnauthorizedUser() {
        final UUID id = UUID.randomUUID();
        final CommentDto commentDto = new CommentDto();
        currentUser.getUser().setRole(UserRole.CONTRIBUTOR);
        final Comment comment = commentFactory.createEntity();
        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));

        commentService.update(UUID.randomUUID(), id, commentDto);
    }

    @Test
    public void delete() {
        final UUID id = UUID.randomUUID();
        final Comment comment = new Comment();
        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));

        commentService.delete(id);

        verify(commentRepository).delete(comment);
    }

    @Test(expected = LegalValidationException.class)
    public void deleteUnauthorizedUser() {
        final UUID id = UUID.randomUUID();
        currentUser.getUser().setRole(UserRole.CONTRIBUTOR);
        final Comment comment = commentFactory.createEntity();
        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));

        commentService.delete(id);
    }

    @Test
    public void findAll() {
        //given
        final UUID nodeId = UUID.randomUUID();
        Pageable pageable = mock(Pageable.class);
        String text = "mockText";
        Comment comment1 = new Comment();
        comment1.setText(text);

        when(commentRepository.findByDocumentNodeIdAndParentIsNullAndStatus(nodeId, CommentStatus.APPROVED, pageable))
                .thenReturn(new PageImpl<>(List.of(comment1)));

        //when
        Page<Comment> all = commentService.findAll(nodeId, pageable);

        //then
        assertEquals("response size should be 1", all.getContent().size(), 1L);
        assertEquals("text is different", all.getContent().get(0).getText(), text);
    }

    @Test
    public void updateCommentStatus() {
        final UUID id = UUID.randomUUID();
        final Comment comment = new Comment();
        comment.setStatus(null);
        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
        commentService.setStatus(id, NEW_STATUS);
        verify(commentRepository).save(commentArgumentCaptor.capture());
        assertThat(commentArgumentCaptor.getValue().getStatus()).isEqualTo(NEW_STATUS);
    }

    @Test(expected = RuntimeException.class)
    public void failUpdateCommentStatus() {
        final UUID id = UUID.randomUUID();
        final Comment comment = new Comment();
        comment.setStatus(CommentStatus.REJECTED);
        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
        commentService.setStatus(id, NEW_STATUS);
    }

    @Test(expected = EntityNotFoundException.class)
    public void attemptToUpdateNonExistingComment() {
        final UUID id = UUID.randomUUID();
        final Comment comment = new Comment();
        comment.setStatus(CommentStatus.REJECTED);
        when(commentRepository.findById(id)).thenReturn(Optional.empty());
        commentService.setStatus(id, NEW_STATUS);
    }

    @Test
    public void findAllReplies() {
        //given
        UUID parentId = UUID.randomUUID();
        Pageable pageable = mock(Pageable.class);
        String text = "mockText";
        Comment comment1 = new Comment();
        comment1.setText(text);

        when(commentRepository.findByParentId(parentId, pageable)).thenReturn(new PageImpl<>(List.of(comment1)));

        //when
        Page<Comment> all = commentService.findAllReplies(parentId, pageable);

        //then
        assertEquals("response size should be 1", all.getContent().size(), 1L);
        assertEquals("text is different", all.getContent().get(0).getText(), text);
    }

    @Test
    public void createReply() {
        UUID parentId = UUID.randomUUID();
        CommentDto commentDto = RandomObjectFiller.createAndFill(CommentDto.class);
        Comment parent = commentFactory.createEntity();
        Comment reply = new Comment();

        when(commentRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(mapperService.map(commentDto)).thenReturn(reply);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        commentService.createReply(parentId, commentDto);

        verify(commentRepository).save(reply);

        assertThat(reply.getOwner()).isEqualTo(currentUser);
        assertThat(reply.getParent()).isEqualTo(parent);
        assertThat(reply.getDocumentNode()).isNull();
    }

    @Test
    public void findAllPendingWithAdminUser() {
        final Pageable pageable = mock(Pageable.class);
        final ApplicationUser adminUser = userFactory.createApplicationUserWithRole(UserRole.ADMIN);
        final Comment comment1 = commentFactory.createEntity();
        final Comment comment2 = commentFactory.createEntity();

        when(currentUserService.getCurrentUser()).thenReturn(adminUser);
        when(commentRepository.findAllByStatus(CommentStatus.PENDING, pageable))
                .thenReturn(new PageImpl<>(List.of(comment1, comment2)));

        final Page<Comment> pendingComments = commentService.findAllPending(pageable);

        assertThat(pendingComments.getTotalElements()).isEqualTo(2);
        assertThat(pendingComments.getContent()).hasSize(2);
        assertThat(pendingComments.getContent()).containsExactly(comment1, comment2);
    }

    @Test
    public void findAllPendingWithOwnerUser() {
        final Pageable pageable = mock(Pageable.class);
        final ApplicationUser ownerUser = userFactory.createApplicationUserWithRole(UserRole.OWNER);
        final Comment comment1 = commentFactory.createEntity();
        final Comment comment2 = commentFactory.createEntity();

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(commentRepository.findAllByStatus(CommentStatus.PENDING, pageable))
                .thenReturn(new PageImpl<>(List.of(comment1, comment2)));

        final Page<Comment> pendingComments = commentService.findAllPending(pageable);

        assertThat(pendingComments.getTotalElements()).isEqualTo(2);
        assertThat(pendingComments.getContent()).hasSize(2);
        assertThat(pendingComments.getContent()).containsExactly(comment1, comment2);
    }

    @Test
    public void findAllPendingWithRegularUser() {
        final Pageable pageable = mock(Pageable.class);
        final ApplicationUser ownerUser = userFactory.createApplicationUserWithRole(UserRole.CONTRIBUTOR);
        final Comment comment1 = commentFactory.createEntity();
        final Comment comment2 = commentFactory.createEntity();
        final List<UUID> documentNodesIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(documentService.getAllAssignedDocumentsNodeIds(ownerUser.getUser()))
                .thenReturn(documentNodesIds);
        when(commentRepository.findAllByDocumentNode_IdInAndStatus(documentNodesIds, CommentStatus.PENDING, pageable))
                .thenReturn(new PageImpl<>(List.of(comment1, comment2)));

        final Page<Comment> pendingComments = commentService.findAllPending(pageable);

        assertThat(pendingComments.getTotalElements()).isEqualTo(2);
        assertThat(pendingComments.getContent()).hasSize(2);
        assertThat(pendingComments.getContent()).containsExactly(comment1, comment2);
    }
}

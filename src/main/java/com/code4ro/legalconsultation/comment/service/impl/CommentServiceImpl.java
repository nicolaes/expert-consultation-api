package com.code4ro.legalconsultation.comment.service.impl;

import com.code4ro.legalconsultation.authentication.model.persistence.ApplicationUser;
import com.code4ro.legalconsultation.comment.mapper.CommentMapper;
import com.code4ro.legalconsultation.comment.model.dto.CommentDetailDto;
import com.code4ro.legalconsultation.comment.model.dto.CommentDto;
import com.code4ro.legalconsultation.comment.model.persistence.Comment;
import com.code4ro.legalconsultation.comment.model.persistence.CommentStatus;
import com.code4ro.legalconsultation.comment.repository.CommentRepository;
import com.code4ro.legalconsultation.comment.service.CommentService;
import com.code4ro.legalconsultation.core.exception.LegalValidationException;
import com.code4ro.legalconsultation.document.core.service.DocumentService;
import com.code4ro.legalconsultation.document.node.model.persistence.DocumentNode;
import com.code4ro.legalconsultation.document.node.service.DocumentNodeService;
import com.code4ro.legalconsultation.security.service.CurrentUserService;
import com.code4ro.legalconsultation.user.model.persistence.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CurrentUserService currentUserService;
    private final DocumentNodeService documentNodeService;
    private final DocumentService documentService;
    private final CommentMapper mapperService;

    public CommentServiceImpl(final CommentRepository commentRepository,
                              final CurrentUserService currentUserService,
                              final DocumentNodeService documentNodeService,
                              final DocumentService documentService,
                              final CommentMapper mapperService) {
        this.commentRepository = commentRepository;
        this.currentUserService = currentUserService;
        this.documentNodeService = documentNodeService;
        this.documentService = documentService;
        this.mapperService = mapperService;
    }

    @Transactional
    @Override
    public CommentDetailDto update(UUID nodeId, final UUID id, final CommentDto commentDto) {
        Comment comment = commentRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        checkIfAuthorized(comment);

        comment.setText(commentDto.getText());
        comment = commentRepository.save(comment);

        return mapperService.mapToCommentDetailDto(comment);
    }

    @Transactional
    @Override
    public CommentDetailDto create(UUID nodeId, final CommentDto commentDto) {
        final DocumentNode node = documentNodeService.findById(nodeId);

        final ApplicationUser currentUser = currentUserService.getCurrentUser();

        Comment comment = mapperService.map(commentDto);
        comment.setDocumentNode(node);
        comment.setOwner(currentUser);
        comment.setLastEditDateTime(new Date());
        comment.setStatus(CommentStatus.PENDING);
        comment = commentRepository.save(comment);

        return mapperService.mapToCommentDetailDto(comment);
    }

    @Transactional
    @Override
    public CommentDetailDto createReply(UUID parentId, CommentDto commentDto) {
        Comment parent = commentRepository.findById(parentId).orElseThrow(EntityNotFoundException::new);
        ApplicationUser currentUser = currentUserService.getCurrentUser();

        Comment reply = mapperService.map(commentDto);
        reply.setParent(parent);
        reply.setOwner(currentUser);
        reply.setLastEditDateTime(new Date());
        reply.setStatus(CommentStatus.PENDING);
        reply.setDocumentNode(parent.getDocumentNode());

        reply = commentRepository.save(reply);

        return mapperService.mapToCommentDetailDto(reply);
    }

    @Transactional
    @Override
    public void delete(final UUID id) {
        final Comment comment = commentRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        checkIfAuthorized(comment);

        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Comment> findAll(final UUID documentNodeId, final Pageable pageable) {
        return commentRepository
                .findByDocumentNodeIdAndParentIsNullAndStatus(documentNodeId, CommentStatus.APPROVED, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Comment> findAllReplies(UUID parentId, Pageable pageable) {
        return commentRepository.findByParentId(parentId, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public BigInteger count(UUID nodeId) {
        return commentRepository.countByDocumentNodeIdAndStatus(nodeId, CommentStatus.APPROVED);
    }

    @Transactional
    @Override
    public CommentDto setStatus(UUID commentId, CommentStatus status) {
        final Comment comment = commentRepository.findById(commentId).orElseThrow(EntityNotFoundException::new);
        if (comment.getStatus() != null && !CommentStatus.PENDING.equals(comment.getStatus())) {
            throw LegalValidationException.builder()
                    .i18nKey("comment.Status.already.set")
                    .httpStatus(HttpStatus.CONFLICT)
                    .build();
        }
        comment.setStatus(status);
        commentRepository.save(comment);
        return mapperService.map(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Comment findById(UUID id) {
        return this.commentRepository.getOne(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Comment> findAllPending(final Pageable pageable) {
        final ApplicationUser currentUser = currentUserService.getCurrentUser();
        if (isOwner(currentUser) || isAdmin(currentUser)) {
            return getAllPendingComments(pageable);
        } else {
            return getAssignedPendingComments(currentUser, pageable);
        }
    }

    private Page<Comment> getAssignedPendingComments(final ApplicationUser currentUser, final Pageable pageable) {
        final List<UUID> assignedNodeIds = documentService.getAllAssignedDocumentsNodeIds(currentUser.getUser());
        return commentRepository.findAllByDocumentNode_IdInAndStatus(assignedNodeIds, CommentStatus.PENDING, pageable);
    }

    private Page<Comment> getAllPendingComments(final Pageable pageable) {
        return commentRepository.findAllByStatus(CommentStatus.PENDING, pageable);
    }

    private void checkIfAuthorized(Comment comment) {
        final ApplicationUser owner = comment.getOwner();
        final ApplicationUser currentUser = currentUserService.getCurrentUser();
        if (currentUser.getUser().getRole() != UserRole.ADMIN && !Objects.equals(currentUser.getId(), owner.getId())) {
            throw LegalValidationException.builder()
                    .i18nKey("comment.Unauthorized.user")
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    private boolean isOwner(final ApplicationUser currentUser) {
        return UserRole.OWNER.equals(currentUser.getUser().getRole());
    }

    private boolean isAdmin(final ApplicationUser currentUser) {
        return UserRole.ADMIN.equals(currentUser.getUser().getRole());
    }
}

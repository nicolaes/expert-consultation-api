package com.code4ro.legalconsultation.comment.repository;

import com.code4ro.legalconsultation.comment.model.persistence.Comment;
import com.code4ro.legalconsultation.comment.model.persistence.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    Page<Comment> findByDocumentNodeIdAndParentIsNullAndStatus(final UUID nodeId,
                                                               final CommentStatus status,
                                                               final Pageable pageable);

    Page<Comment> findByParentId(UUID parentId, Pageable pageable);

    BigInteger countByDocumentNodeIdAndStatus(final UUID nodeId, final CommentStatus status);

    Page<Comment> findAllByStatus(CommentStatus status, Pageable pageable);

    Page<Comment> findAllByDocumentNode_IdInAndStatus(List<UUID> documentNodeIds,
                                                      CommentStatus status,
                                                      Pageable pageable);
}

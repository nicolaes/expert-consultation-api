package com.code4ro.legalconsultation.comment.service;

import com.code4ro.legalconsultation.comment.model.dto.CommentDetailDto;
import com.code4ro.legalconsultation.comment.model.dto.CommentDto;
import com.code4ro.legalconsultation.comment.model.persistence.Comment;
import com.code4ro.legalconsultation.comment.model.persistence.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigInteger;
import java.util.UUID;

public interface CommentService {
    CommentDetailDto update(UUID nodeId, UUID id, CommentDto commentDto);

    CommentDetailDto create(UUID nodeId, CommentDto commentDto);

    CommentDetailDto createReply(UUID parentId, CommentDto commentDto);

    void delete(UUID id);

    Page<Comment> findAll(UUID nodeId, Pageable pageable);

    Page<Comment> findAllReplies(UUID parentId, Pageable pageable);

    BigInteger count(UUID nodeId);

    CommentDto setStatus(UUID commentId, CommentStatus approved);

    Comment findById(UUID id);

    Page<Comment> findAllPending(Pageable pageable);
}

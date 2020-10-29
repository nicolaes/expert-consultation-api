package com.code4ro.legalconsultation.comment.factory;

import com.code4ro.legalconsultation.comment.model.dto.CommentDetailDto;
import com.code4ro.legalconsultation.comment.model.dto.CommentDto;
import com.code4ro.legalconsultation.comment.model.persistence.Comment;
import com.code4ro.legalconsultation.comment.model.persistence.CommentStatus;
import com.code4ro.legalconsultation.comment.service.CommentService;
import com.code4ro.legalconsultation.core.factory.RandomObjectFiller;
import com.code4ro.legalconsultation.authentication.model.persistence.ApplicationUser;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public final class CommentFactory {

    @Autowired
    private CommentService commentService;

    public CommentDto create() {
        final CommentDto commentDto = RandomObjectFiller.createAndFill(CommentDto.class);
        if (commentDto == null) throw new IllegalArgumentException("Failed to create the comment");
        commentDto.setLastEditDateTime(new Date());
        commentDto.setStatus(CommentStatus.PENDING);
        return commentDto;
    }

    public CommentDetailDto save(final UUID nodeId) {
        return commentService.create(nodeId, create());
    }

    public CommentDetailDto saveReply(final UUID parentId) {
        return commentService.createReply(parentId, create());
    }

    public Comment createEntity() {
        final Comment comment = new Comment();
        comment.setOwner(RandomObjectFiller.createAndFillWithBaseEntity(ApplicationUser.class));
        comment.setText(RandomStringUtils.randomAlphanumeric(10));
        comment.setId(UUID.randomUUID());
        return comment;
    }
}

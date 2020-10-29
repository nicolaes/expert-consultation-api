package com.code4ro.legalconsultation.comment.controller;

import com.code4ro.legalconsultation.comment.mapper.CommentMapper;
import com.code4ro.legalconsultation.comment.model.dto.CommentDetailDto;
import com.code4ro.legalconsultation.comment.model.dto.CommentDto;
import com.code4ro.legalconsultation.comment.model.persistence.Comment;
import com.code4ro.legalconsultation.comment.service.CommentService;
import com.code4ro.legalconsultation.core.model.dto.PageDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.code4ro.legalconsultation.comment.model.persistence.CommentStatus.APPROVED;
import static com.code4ro.legalconsultation.comment.model.persistence.CommentStatus.REJECTED;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @ApiOperation(value = "Get all pending comments for approval",
            response = PageDto.class,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/pending")
    public ResponseEntity<PageDto<CommentDetailDto>> findAllPending(@ApiParam("Page object information being requested") final Pageable pageable) {
        final Page<Comment> comments = commentService.findAllPending(pageable);
        final Page<CommentDetailDto> commentsDto = comments.map(commentMapper::mapToCommentDetailDto);

        return ResponseEntity.ok(new PageDto<>(commentsDto));
    }

    @ApiOperation(value = "Approve pending comment",
            response = PageDto.class,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/{commentId}/approve")
    public ResponseEntity<CommentDto> approve(@PathVariable UUID commentId) {
        return ResponseEntity.ok(commentService.setStatus(commentId, APPROVED));
    }

    @ApiOperation(value = "Reject pending comment",
            response = PageDto.class,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/{commentId}/reject")
    public ResponseEntity<CommentDto> reject(@PathVariable UUID commentId) {
        return ResponseEntity.ok(commentService.setStatus(commentId, REJECTED));
    }
}

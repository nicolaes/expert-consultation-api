package com.code4ro.legalconsultation.comment.controller;

import com.code4ro.legalconsultation.comment.mapper.CommentMapper;
import com.code4ro.legalconsultation.comment.model.dto.CommentDetailDto;
import com.code4ro.legalconsultation.comment.model.dto.CommentDto;
import com.code4ro.legalconsultation.comment.model.dto.CommentForChatDto;
import com.code4ro.legalconsultation.comment.model.persistence.Comment;
import com.code4ro.legalconsultation.comment.service.CommentService;
import com.code4ro.legalconsultation.core.model.dto.PageDto;
import com.code4ro.legalconsultation.core.model.persistence.BaseEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documentnodes/{nodeId}/comments")
@Api(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DocumentCommentsController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @ApiOperation(value = "Create a new comment in the platform",
            response = CommentDto.class,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping
    public ResponseEntity<CommentDetailDto> create(@ApiParam(value = "The id of the node") @PathVariable final UUID nodeId,
                                                   @ApiParam("The DTO object containing a new comment") @RequestBody final CommentDto commentDto) {
        CommentDetailDto comment = commentService.create(nodeId, commentDto);
        return ResponseEntity.ok(comment);
    }

    @ApiOperation(value = "Update a comment in the platform",
            response = CommentDto.class,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PutMapping("/{id}")
    public ResponseEntity<CommentDetailDto> update(@ApiParam(value = "The id of the node") @PathVariable final UUID nodeId,
                                                   @ApiParam(value = "The id of the comment") @PathVariable final UUID id,
                                                   @ApiParam("The DTO object to update") @RequestBody final CommentDto commentDto) {
        return ResponseEntity.ok(commentService.update(nodeId, id, commentDto));
    }

    @ApiOperation(value = "Delete a comment",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@ApiParam(value = "The id of the comment") @PathVariable final UUID id) {
        commentService.delete(id);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("Get all comments of a node")
    @GetMapping
    public ResponseEntity<PageDto<CommentForChatDto>> findAll(@ApiParam(value = "The id of the node") @PathVariable final UUID nodeId,
                                                             @ApiParam("Page object information being requested") final Pageable pageable) {
        Page<Comment> comments = commentService.findAll(nodeId, pageable);
        List<UUID> commentIds = comments.map(BaseEntity::getId).getContent();
        List<Comment> allReplies = commentService.findRepliesForComments(commentIds);
        Page<CommentForChatDto> commentsDto = comments.map(comment -> {
            List<Comment> repliesForComment = allReplies
                    .stream()
                    .filter(r -> r.getParent().getId().equals(comment.getId()))
                    .collect(Collectors.toList());
            return commentMapper.mapToCommentForChatDto(comment, repliesForComment);
        });

        return ResponseEntity.ok(new PageDto<>(commentsDto));
    }

    @ApiOperation(value = "Get all replies of a comment",
            response = PageDto.class,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<PageDto<CommentDetailDto>> findAllReplies(@ApiParam(value = "The id of the comment") @PathVariable final UUID commentId,
                                                                    @ApiParam("Page object information being requested") final Pageable pageable) {
        Page<Comment> replies = commentService.findAllReplies(commentId, pageable);
        Page<CommentDetailDto> repliesDto = replies.map(commentMapper::mapToCommentDetailDto);

        return ResponseEntity.ok(new PageDto<>(repliesDto));
    }

    @ApiOperation(value = "Create a new reply in the platform",
            response = CommentDto.class,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/{commentId}/replies")
    public ResponseEntity<CommentDetailDto> createReply(@ApiParam(value = "The id of the comment") @PathVariable final UUID commentId,
                                                        @ApiParam("The DTO object containing a new reply") @RequestBody CommentDto commentDto) {
        return ResponseEntity.ok(commentService.createReply(commentId, commentDto));
    }
}

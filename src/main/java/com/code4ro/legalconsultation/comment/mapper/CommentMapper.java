package com.code4ro.legalconsultation.comment.mapper;

import com.code4ro.legalconsultation.comment.model.dto.*;
import com.code4ro.legalconsultation.comment.model.persistence.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CommentDocumentDataMapper.class})
public interface CommentMapper {
    CommentDto map(Comment comment);

    Comment map(CommentDto commentDto);

    @Mappings( {
            @Mapping(source = "comment.owner.name", target = "user"),
    })
    CommentWithUserDto mapToCommentWithUserDto(Comment comment);

    @Mappings( {
            @Mapping(target = "documentTitle", ignore = true),
            @Mapping(target = "nodeTitle", ignore = true),
            @Mapping(target = "nodeContent", ignore = true),
            @Mapping(source = "comment.owner.name", target = "user"),
    })
    CommentDetailDto mapToCommentDetailDto(Comment comment);

    @Mappings( {
            @Mapping(target = "voteCount", ignore = true),
            @Mapping(target = "myVote", ignore = true),
            @Mapping(source = "replies", target = "replies"),
            @Mapping(source = "comment.owner.name", target = "user"),
    })
    CommentForChatDto mapToCommentForChatDto(Comment comment, List<Comment> replies);
}

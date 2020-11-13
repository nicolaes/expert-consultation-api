package com.code4ro.legalconsultation.comment.mapper;

import com.code4ro.legalconsultation.comment.model.dto.CommentDetailDto;
import com.code4ro.legalconsultation.comment.model.dto.CommentDto;
import com.code4ro.legalconsultation.comment.model.persistence.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {CommentDocumentDataMapper.class})
public interface CommentMapper {
    CommentDto map(Comment comment);

    Comment map(CommentDto commentDto);

    @Mappings( {
            @Mapping(target = "user", source = "comment.owner.name"),
    })
    CommentDetailDto mapToCommentDetailDto(Comment comment);
}

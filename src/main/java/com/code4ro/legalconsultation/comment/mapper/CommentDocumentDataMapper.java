package com.code4ro.legalconsultation.comment.mapper;

import com.code4ro.legalconsultation.comment.model.dto.CommentDetailDto;
import com.code4ro.legalconsultation.comment.model.dto.CommentForChatDto;
import com.code4ro.legalconsultation.comment.model.persistence.Comment;
import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.document.core.service.DocumentService;
import com.code4ro.legalconsultation.vote.service.VoteService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import static java.lang.Integer.min;

@Mapper(componentModel = "spring")
public abstract class CommentDocumentDataMapper {
    private static final int DOCUMENT_CONTENT_IN_COMMENT_LENGTH = 100;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private VoteService voteService;

    @AfterMapping
    void afterDetailMapping(@MappingTarget CommentDetailDto dto, Comment entity) {
        final String nodeContent = entity.getDocumentNode().getContent();
        dto.setNodeContent(nodeContent != null ?
                nodeContent.substring(0, min(DOCUMENT_CONTENT_IN_COMMENT_LENGTH, nodeContent.length())) : "");
        dto.setNodeTitle(entity.getDocumentNode().getTitle() != null ? entity.getDocumentNode().getTitle() : "");

        final DocumentConsolidated documentConsolidated = documentService.getDocumentConsolidatedForComment(entity);
        dto.setDocumentTitle(documentConsolidated.getDocumentMetadata().getDocumentTitle());
    }

    @AfterMapping
    void afterCommentForChatMapping(@MappingTarget CommentForChatDto dto, Comment comment) {
        dto.setVoteCount(voteService.getVoteCountForComment(comment.getId()));
        dto.setMyVote(voteService.getVoteForComment(comment.getId()));
    }
}

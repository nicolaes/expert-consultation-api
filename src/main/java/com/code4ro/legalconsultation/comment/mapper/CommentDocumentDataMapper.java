package com.code4ro.legalconsultation.comment.mapper;

import com.code4ro.legalconsultation.comment.model.dto.CommentDetailDto;
import com.code4ro.legalconsultation.comment.model.persistence.Comment;
import com.code4ro.legalconsultation.document.consolidated.model.persistence.DocumentConsolidated;
import com.code4ro.legalconsultation.document.core.service.DocumentService;
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

    @AfterMapping
    void afterDetailMapping(@MappingTarget CommentDetailDto dto, Comment entity) {
        final String nodeContent = entity.getDocumentNode().getContent();
        dto.setNodeContent(nodeContent != null ?
                nodeContent.substring(0, min(DOCUMENT_CONTENT_IN_COMMENT_LENGTH, nodeContent.length())) : "");
        dto.setNodeTitle(entity.getDocumentNode().getTitle() != null ? entity.getDocumentNode().getTitle() : "");

        final DocumentConsolidated documentConsolidated = documentService.getDocumentConsolidatedForComment(entity);
        dto.setDocumentTitle(documentConsolidated.getDocumentMetadata().getDocumentTitle());
    }
}

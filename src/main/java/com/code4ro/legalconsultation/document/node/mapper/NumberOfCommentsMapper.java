package com.code4ro.legalconsultation.document.node.mapper;

import com.code4ro.legalconsultation.comment.model.persistence.CommentStatus;
import com.code4ro.legalconsultation.comment.repository.CommentRepository;
import com.code4ro.legalconsultation.document.node.model.dto.DocumentNodeDto;
import com.code4ro.legalconsultation.document.node.model.persistence.DocumentNode;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class NumberOfCommentsMapper {

    @Autowired
    private CommentRepository commentRepository;

    @AfterMapping
    public void computeNumberOfComments(@MappingTarget DocumentNodeDto dto, DocumentNode model) {
        dto.setNumberOfComments(commentRepository.countByDocumentNodeIdAndStatus(model.getId(), CommentStatus.APPROVED));
    }
}



package com.code4ro.legalconsultation.comment.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDetailDto extends CommentDto {
    private String user;
    private String documentTitle;
    private String nodeTitle;
    private String nodeContent;
}

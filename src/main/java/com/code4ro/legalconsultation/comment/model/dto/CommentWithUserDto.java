package com.code4ro.legalconsultation.comment.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CommentWithUserDto extends CommentDto {
    private String user;
}

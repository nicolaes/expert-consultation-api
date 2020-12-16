package com.code4ro.legalconsultation.comment.model.dto;

import com.code4ro.legalconsultation.vote.model.persistence.VoteType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CommentForChatDto extends CommentWithUserDto {
    Map<VoteType, Long> voteCount;
    VoteType myVote;
}

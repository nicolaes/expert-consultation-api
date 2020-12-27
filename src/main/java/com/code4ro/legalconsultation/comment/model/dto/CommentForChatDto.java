package com.code4ro.legalconsultation.comment.model.dto;

import com.code4ro.legalconsultation.vote.model.dto.VoteDto;
import com.code4ro.legalconsultation.vote.model.persistence.VoteType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CommentForChatDto extends CommentWithUserAbstract {
    Map<VoteType, Long> voteCount;
    VoteDto myVote;
    List<CommentWithUserDto> replies;
}

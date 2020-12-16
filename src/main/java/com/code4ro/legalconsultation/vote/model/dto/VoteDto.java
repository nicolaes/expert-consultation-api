package com.code4ro.legalconsultation.vote.model.dto;

import com.code4ro.legalconsultation.vote.model.persistence.VoteType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * The convention is that:<br>
 * <ul>
 * <li>An explicit <b>true</b> vote up will mean a vote up.
 * <li>An explicit <b>false</b> vote up will mean a vote down.
 * <li>An explicit <b>null</b> vote up will mean a vote reset (either delete or
 * nullify all the votes so far - this is to be decided).
 * </ul>
 * TODO: clarify the policy for vote resetting...
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = {"id", "commentId"})
public class VoteDto {
    private UUID id;
    private UUID commentId;
    private VoteType vote;

    public VoteDto(UUID id, UUID commentId, VoteType vote) {
        this.id = id;
        this.commentId = commentId;
        this.vote = vote;
    }
}

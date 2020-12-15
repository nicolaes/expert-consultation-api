package com.code4ro.legalconsultation.vote.repository;

import com.code4ro.legalconsultation.vote.model.persistence.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {
    Vote findByCommentIdAndOwnerId(final UUID commentId, final UUID ownerId);

    List<Vote> findAllByCommentId(final UUID commentId);
}

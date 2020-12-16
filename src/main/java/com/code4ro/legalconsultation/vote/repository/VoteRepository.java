package com.code4ro.legalconsultation.vote.repository;

import com.code4ro.legalconsultation.vote.model.dto.VoteDto;
import com.code4ro.legalconsultation.vote.model.persistence.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {
    Vote findByCommentIdAndOwnerId(final UUID commentId, final UUID ownerId);

    List<Vote> findAllByCommentId(final UUID commentId);

    @Query("SELECT v.vote, COUNT(v) AS count " +
            "FROM Vote v " +
            "WHERE v.comment.id = :commentId " +
            "GROUP BY v.vote")
    List<Tuple> findVoteCountByUserForComment(@Param("commentId") final UUID commentId);

    @Query("SELECT new com.code4ro.legalconsultation.vote.model.dto.VoteDto(v.id, v.comment.id, v.vote) " +
            "FROM Vote v " +
            "WHERE v.comment.id = :commentId AND v.owner.id = :ownerId")
    Optional<VoteDto> findVoteByOwnerIdAndCommentId(@Param("ownerId") UUID owner_id, @Param("commentId") UUID comment_id);
}

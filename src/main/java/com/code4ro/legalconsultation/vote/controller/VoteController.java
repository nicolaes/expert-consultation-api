package com.code4ro.legalconsultation.vote.controller;

import com.code4ro.legalconsultation.vote.model.dto.VoteDto;
import com.code4ro.legalconsultation.vote.model.persistence.VoteType;
import com.code4ro.legalconsultation.vote.service.VoteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(value = "/api/votes")
@RequiredArgsConstructor
@Api(produces = MediaType.APPLICATION_JSON_VALUE)
public class VoteController {

    private final VoteService voteService;

    @ApiOperation(value = "Return anonymous vote count for a comment")
    @GetMapping("/{commentId}")
    public ResponseEntity<Map<VoteType, Long>> getAnonymousVotesFor(@PathVariable("commentId") UUID commentId) {
        return ok(voteService.getVoteCountForComment(commentId));
    }

    @ApiOperation("Create vote for a comment")
    @PutMapping
    public ResponseEntity<VoteDto> updateVote(@Valid @RequestBody VoteDto voteDtoReq) {
        return ok(voteService.vote(voteDtoReq));
    }

    @ApiOperation("Update vote for comment")
    @PostMapping
    public ResponseEntity<VoteDto> saveVote(@Valid @RequestBody VoteDto voteDtoReq) {
        return ok(voteService.vote(voteDtoReq));
    }
}

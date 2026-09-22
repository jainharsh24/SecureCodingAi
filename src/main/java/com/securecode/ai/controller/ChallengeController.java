package com.securecode.ai.controller;

import com.securecode.ai.dto.ChallengeRequest;
import com.securecode.ai.dto.ChallengeResponse;
import com.securecode.ai.service.ChallengeService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/challenges")
public class ChallengeController {
    private final ChallengeService challengeService;
    public ChallengeController(ChallengeService challengeService) { this.challengeService = challengeService; }
    @GetMapping public List<ChallengeResponse> getAll() { return challengeService.getAllForStudent(); }
    @GetMapping("/{id}") public ChallengeResponse getById(@PathVariable Long id) { return challengeService.getByIdForStudent(id); }
    @PostMapping public ResponseEntity<ChallengeResponse> create(@Valid @RequestBody ChallengeRequest request) {
        ChallengeResponse response = challengeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).location(URI.create("/challenges/" + response.id())).body(response);
    }
    @PutMapping("/{id}") public ChallengeResponse update(@PathVariable Long id, @Valid @RequestBody ChallengeRequest request) {
        return challengeService.update(id, request);
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
        challengeService.delete(id); return ResponseEntity.noContent().build();
    }
}

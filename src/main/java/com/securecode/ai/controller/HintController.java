package com.securecode.ai.controller;

import com.securecode.ai.dto.HintResponse;
import com.securecode.ai.dto.HintSummaryResponse;
import com.securecode.ai.entity.HintAssistanceLevel;
import com.securecode.ai.service.HintService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/challenges/{challengeId}/hints")
public class HintController {
    private final HintService hints;
    public HintController(HintService hints) { this.hints = hints; }
    @GetMapping public List<HintSummaryResponse> available(@PathVariable Long challengeId, Authentication authentication) {
        return hints.available(challengeId, authentication.getName());
    }
    @PostMapping("/{level}/view")
    public HintResponse view(@PathVariable Long challengeId, @PathVariable HintAssistanceLevel level, Authentication authentication) {
        return hints.view(challengeId, authentication.getName(), level);
    }
}

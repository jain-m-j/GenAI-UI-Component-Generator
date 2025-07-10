package com.design.uigenerartor.controller;

import com.design.uigenerartor.model.CssResponse;
import com.design.uigenerartor.model.DesignPrompt;
import com.design.uigenerartor.service.GeminiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/generate")
@CrossOrigin(origins = "http://localhost:3000")
public class CssGenController {

    private final GeminiService geminiService;


    public CssGenController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/css")
    public Mono<ResponseEntity<CssResponse>> generateCss(@RequestBody DesignPrompt designPrompt) {
        if (designPrompt == null || designPrompt.getPrompt() == null || designPrompt.getPrompt().trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(new CssResponse(null, "Design prompt cannot be empty.")));
        }

        return geminiService.generateCss(designPrompt.getPrompt())
                .map(cssCode -> ResponseEntity.ok(new CssResponse(cssCode)))
                .onErrorResume(e -> {
                    System.err.println("Error generating CSS: " + e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new CssResponse(null, "Failed to generate CSS: " + e.getMessage())));
                });
    }
}


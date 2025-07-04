package com.cagri.hrms.controller;

import com.cagri.hrms.service.PublicApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/publicapi")
@RequiredArgsConstructor
public class PublicApiController {

    private final PublicApiService publicApiService;

    @GetMapping("/homepage-content")
    public ResponseEntity<String> getHomepageContent() {
        return ResponseEntity.ok(publicApiService.getHomepageContent());
    }

    @GetMapping("/platform-features")
    public ResponseEntity<String> getPlatformFeatures() {
        return ResponseEntity.ok(publicApiService.getPlatformFeatures());
    }

    @GetMapping("/how-it-works")
    public ResponseEntity<String> getHowItWorks() {
        return ResponseEntity.ok(publicApiService.getHowItWorks());
    }

    @GetMapping("/holidays")
    public ResponseEntity<String> getHolidays() {
        return ResponseEntity.ok(publicApiService.getHolidays());
    }
}


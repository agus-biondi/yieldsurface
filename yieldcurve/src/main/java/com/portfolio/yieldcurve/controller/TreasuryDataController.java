package com.portfolio.yieldcurve.controller;

import com.portfolio.yieldcurve.service.TreasuryDataUpdaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TreasuryDataController {

    @Value("${cronjob.token}")
    private String token;

    @Autowired
    private TreasuryDataUpdaterService treasuryDataUpdaterService;

    @GetMapping("/trigger-update")
    public ResponseEntity<String> triggerUpdate(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String extractedToken = authorization.substring(7);
        if (!extractedToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        treasuryDataUpdaterService.updateDataFromTreasury();
        return ResponseEntity.ok("Updated Triggered");
    }

}

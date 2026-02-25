package com.axelfrache.signalbroker.controller;

import com.axelfrache.signalbroker.dto.StatsOverviewDto;
import com.axelfrache.signalbroker.service.LabeledTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatsController {

    private final LabeledTicketService service;

    @GetMapping("/overview")
    public ResponseEntity<StatsOverviewDto> getOverview(
            @RequestParam(required = false) Double from,
            @RequestParam(required = false) Double to) {

        return ResponseEntity.ok(service.getStats(from, to));
    }
}

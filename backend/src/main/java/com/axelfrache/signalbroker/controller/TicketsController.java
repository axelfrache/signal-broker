package com.axelfrache.signalbroker.controller;

import com.axelfrache.signalbroker.dto.PaginatedResponse;
import com.axelfrache.signalbroker.dto.TicketDetailsDto;
import com.axelfrache.signalbroker.dto.TicketDto;
import com.axelfrache.signalbroker.service.LabeledTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TicketsController {

    private final LabeledTicketService service;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "receivedAt",
            "labeledAt",
            "confidence",
            "priority",
            "category",
            "ticketId");

    @GetMapping
    public ResponseEntity<PaginatedResponse<TicketDto>> getTickets(
            @RequestParam(required = false) List<String> priority,
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Double from,
            @RequestParam(required = false) Double to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "receivedAt") String sort,
            @RequestParam(defaultValue = "desc") String dir) {

        var safeSize = Math.clamp(size, 1, 100);
        var safePage = Math.max(0, page);
        var safeSort = ALLOWED_SORT_FIELDS.contains(sort) ? sort : "receivedAt";
        var safeDir = "asc".equalsIgnoreCase(dir) ? "asc" : "desc";

        return ResponseEntity.ok(service.getTickets(priority, category, q, from, to, safePage, safeSize, safeSort, safeDir));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDetailsDto> getTicket(@PathVariable String ticketId) {
        return service.getTicketById(ticketId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

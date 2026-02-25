package com.axelfrache.signalbroker.controller;

import com.axelfrache.signalbroker.dto.PaginatedResponse;
import com.axelfrache.signalbroker.dto.TicketDetailsDto;
import com.axelfrache.signalbroker.dto.TicketDto;
import com.axelfrache.signalbroker.service.LabeledTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TicketsController {

    private final LabeledTicketService service;

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

        if (size > 100)
            size = 100;
        return ResponseEntity.ok(service.getTickets(priority, category, q, from, to, page, size, sort, dir));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDetailsDto> getTicket(@PathVariable String ticketId) {
        var ticket = service.getTicketById(ticketId);
        if (ticket == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(ticket);
    }
}

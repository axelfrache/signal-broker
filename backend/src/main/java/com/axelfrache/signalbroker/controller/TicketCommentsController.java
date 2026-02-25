package com.axelfrache.signalbroker.controller;

import com.axelfrache.signalbroker.dto.CreateTicketCommentRequest;
import com.axelfrache.signalbroker.dto.TicketCommentDto;
import com.axelfrache.signalbroker.service.TicketCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TicketCommentsController {

    private final TicketCommentService ticketCommentService;

    @GetMapping
    public List<TicketCommentDto> listComments(@PathVariable UUID ticketId) {
        return ticketCommentService.listComments(ticketId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketCommentDto addComment(
            @PathVariable UUID ticketId,
            @Valid @RequestBody CreateTicketCommentRequest req) {
        return ticketCommentService.addComment(ticketId, req);
    }
}

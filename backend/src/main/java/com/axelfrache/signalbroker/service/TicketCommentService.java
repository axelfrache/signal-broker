package com.axelfrache.signalbroker.service;

import com.axelfrache.signalbroker.dto.CreateTicketCommentRequest;
import com.axelfrache.signalbroker.dto.TicketCommentDto;
import com.axelfrache.signalbroker.model.TicketCommentEntity;
import com.axelfrache.signalbroker.repository.TicketCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketCommentService {

    private final TicketCommentRepository repository;

    public List<TicketCommentDto> listComments(UUID ticketId) {
        return repository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public TicketCommentDto addComment(UUID ticketId, CreateTicketCommentRequest req) {
        log.info("Adding comment to ticket {}", ticketId);
        var entity = TicketCommentEntity.builder()
                .id(UUID.randomUUID())
                .ticketId(ticketId)
                .authorName(req.authorName())
                .body(req.body())
                .createdAt(Instant.now())
                .schemaVersion(1)
                .build();

        var saved = repository.save(entity);
        return toDto(saved);
    }

    private TicketCommentDto toDto(TicketCommentEntity entity) {
        return new TicketCommentDto(
                entity.getId(),
                entity.getTicketId(),
                entity.getAuthorName(),
                entity.getBody(),
                entity.getCreatedAt());
    }
}

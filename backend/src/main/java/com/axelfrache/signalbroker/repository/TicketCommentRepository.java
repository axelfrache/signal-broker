package com.axelfrache.signalbroker.repository;

import com.axelfrache.signalbroker.model.TicketCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketCommentRepository extends JpaRepository<TicketCommentEntity, UUID> {
    List<TicketCommentEntity> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);
}

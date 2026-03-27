package com.ticket.classifier.repository;

import com.ticket.classifier.entity.Ticket;
import com.ticket.classifier.enums.TicketStatus;
import com.ticket.classifier.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreatedBy(User user);
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByAssignedAgent(User agent);
    List<Ticket> findByFlaggedForReviewTrue();
}
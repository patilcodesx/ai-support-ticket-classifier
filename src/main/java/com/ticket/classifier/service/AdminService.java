package com.ticket.classifier.service;

import com.ticket.classifier.dto.response.TicketResponse;
import com.ticket.classifier.entity.Ticket;
import com.ticket.classifier.entity.User;
import com.ticket.classifier.enums.TicketStatus;
import com.ticket.classifier.repository.TicketRepository;
import com.ticket.classifier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketService ticketService;

    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<TicketResponse> getFlaggedTickets() {
        return ticketRepository.findByFlaggedForReviewTrue()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<TicketResponse> getTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatus(status)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<User> getAllAgents() {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getRole().name().equals("AGENT"))
                .collect(Collectors.toList());
    }

    public TicketResponse assignTicketToAgent(Long ticketId, Long agentId) {
        return ticketService.assignTicket(ticketId, agentId);
    }

    public TicketResponse resolveTicket(Long ticketId) {
        return ticketService.resolveTicket(ticketId);
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        TicketResponse res = new TicketResponse();
        res.setId(ticket.getId());
        res.setTitle(ticket.getTitle());
        res.setDescription(ticket.getDescription());
        res.setCategory(ticket.getCategory());
        res.setPriority(ticket.getPriority());
        res.setStatus(ticket.getStatus());
        res.setAssignedTeam(ticket.getAssignedTeam());
        res.setSuggestedResponse(ticket.getSuggestedResponse());
        res.setConfidenceScore(ticket.getConfidenceScore());
        res.setFlaggedForReview(ticket.isFlaggedForReview());
        res.setCreatedBy(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getEmail() : null);
        res.setAssignedAgent(ticket.getAssignedAgent() != null ? ticket.getAssignedAgent().getEmail() : null);
        res.setCreatedAt(ticket.getCreatedAt());
        res.setResolvedAt(ticket.getResolvedAt());
        return res;
    }
}
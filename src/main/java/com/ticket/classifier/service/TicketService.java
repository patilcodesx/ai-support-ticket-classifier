package com.ticket.classifier.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket.classifier.dto.request.TicketRequest;
import com.ticket.classifier.dto.response.TicketResponse;
import com.ticket.classifier.entity.Ticket;
import com.ticket.classifier.entity.User;
import com.ticket.classifier.enums.*;
import com.ticket.classifier.repository.TicketRepository;
import com.ticket.classifier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TicketResponse createTicket(TicketRequest request) {
        User currentUser = getCurrentUser();

        // Call Gemini to classify
        String geminiRaw = geminiService.classify(request.getTitle(), request.getDescription());
        String geminiJson = cleanJson(geminiRaw);

        Category category = Category.TECHNICAL;
        Priority priority = Priority.MEDIUM;
        Team assignedTeam = Team.TECHNICAL;
        String suggestedResponse = "We have received your ticket and will respond shortly.";
        int confidenceScore = 80;
        boolean flaggedForReview = false;

        try {
            JsonNode node = objectMapper.readTree(geminiJson);
            category = Category.valueOf(node.path("category").asText("TECHNICAL"));
            priority = Priority.valueOf(node.path("priority").asText("MEDIUM"));
            assignedTeam = Team.valueOf(node.path("assignedTeam").asText("TECHNICAL"));
            suggestedResponse = node.path("suggestedResponse").asText(suggestedResponse);
            confidenceScore = node.path("confidenceScore").asInt(80);
            flaggedForReview = node.path("flaggedForReview").asBoolean(false);
        } catch (Exception e) {
            flaggedForReview = true; // fallback: flag if parse fails
        }

        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(category)
                .priority(priority)
                .assignedTeam(assignedTeam)
                .suggestedResponse(suggestedResponse)
                .confidenceScore(confidenceScore)
                .flaggedForReview(flaggedForReview)
                .status(TicketStatus.OPEN)
                .createdBy(currentUser)
                .build();

        Ticket saved = ticketRepository.save(ticket);

        // Send confirmation email to customer
        emailService.sendTicketConfirmation(currentUser.getEmail(), currentUser.getName(), saved);

        return mapToResponse(saved);
    }

    public List<TicketResponse> getMyTickets() {
        User currentUser = getCurrentUser();
        return ticketRepository.findByCreatedBy(currentUser)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + id));
        return mapToResponse(ticket);
    }

    public TicketResponse resolveTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + id));
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());
        return mapToResponse(ticketRepository.save(ticket));
    }

    public TicketResponse assignTicket(Long ticketId, Long agentId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found: " + agentId));
        ticket.setAssignedAgent(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        return mapToResponse(ticketRepository.save(ticket));
    }

    private String cleanJson(String raw) {
        // Remove markdown code fences if Gemini wraps in ```json ... ```
        return raw.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*", "").trim();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
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
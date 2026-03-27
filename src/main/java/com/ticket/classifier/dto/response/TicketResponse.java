package com.ticket.classifier.dto.response;

import com.ticket.classifier.enums.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TicketResponse {
    private Long id;
    private String title;
    private String description;
    private Category category;
    private Priority priority;
    private TicketStatus status;
    private Team assignedTeam;
    private String suggestedResponse;
    private Integer confidenceScore;
    private boolean flaggedForReview;
    private String createdBy;
    private String assignedAgent;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
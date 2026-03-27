package com.ticket.classifier.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
}
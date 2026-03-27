package com.ticket.classifier.controller;

import com.ticket.classifier.dto.response.TicketResponse;
import com.ticket.classifier.entity.User;
import com.ticket.classifier.enums.TicketStatus;
import com.ticket.classifier.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/tickets")
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        return ResponseEntity.ok(adminService.getAllTickets());
    }

    @GetMapping("/tickets/flagged")
    public ResponseEntity<List<TicketResponse>> getFlaggedTickets() {
        return ResponseEntity.ok(adminService.getFlaggedTickets());
    }

    @GetMapping("/tickets/status/{status}")
    public ResponseEntity<List<TicketResponse>> getByStatus(@PathVariable TicketStatus status) {
        return ResponseEntity.ok(adminService.getTicketsByStatus(status));
    }

    @GetMapping("/agents")
    public ResponseEntity<List<User>> getAllAgents() {
        return ResponseEntity.ok(adminService.getAllAgents());
    }

    @PutMapping("/tickets/{ticketId}/assign/{agentId}")
    public ResponseEntity<TicketResponse> assignTicket(@PathVariable Long ticketId,
                                                        @PathVariable Long agentId) {
        return ResponseEntity.ok(adminService.assignTicketToAgent(ticketId, agentId));
    }

    @PutMapping("/tickets/{ticketId}/resolve")
    public ResponseEntity<TicketResponse> resolveTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(adminService.resolveTicket(ticketId));
    }
}
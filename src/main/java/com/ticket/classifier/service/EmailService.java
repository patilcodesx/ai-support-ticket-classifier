package com.ticket.classifier.service;

import com.ticket.classifier.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendTicketConfirmation(String toEmail, String customerName, Ticket ticket) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Ticket #" + ticket.getId() + " Received — " + ticket.getTitle());
        message.setText("""
                Hi %s,

                Your support ticket has been received and classified.

                Ticket ID   : #%d
                Title       : %s
                Category    : %s
                Priority    : %s
                Assigned To : %s Team
                Status      : %s

                Our suggested response:
                "%s"

                We will get back to you shortly.

                — Support Team
                """.formatted(
                customerName,
                ticket.getId(),
                ticket.getTitle(),
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getAssignedTeam(),
                ticket.getStatus(),
                ticket.getSuggestedResponse()
        ));
        mailSender.send(message);
    }

    public void sendResolutionNotice(String toEmail, String customerName, Ticket ticket) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Ticket #" + ticket.getId() + " Resolved");
        message.setText("""
                Hi %s,

                Your ticket #%d ("%s") has been marked as RESOLVED.

                Thank you for reaching out. If you have further questions, feel free to raise a new ticket.

                — Support Team
                """.formatted(customerName, ticket.getId(), ticket.getTitle()));
        mailSender.send(message);
    }
}
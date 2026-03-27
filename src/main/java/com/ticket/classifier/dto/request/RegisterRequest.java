package com.ticket.classifier.dto.request;

import com.ticket.classifier.enums.Role;
import com.ticket.classifier.enums.Team;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;
    @Email @NotBlank
    private String email;
    @NotBlank
    private String password;
    private Role role;
    private Team team;
}
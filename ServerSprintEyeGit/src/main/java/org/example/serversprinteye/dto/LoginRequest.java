package org.example.serversprinteye.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class LoginRequest {
    //email or username for login
    @NotEmpty(message = "Podaj login lub email.")
    private String identifier;
    @NotEmpty(message = "Podaj hasło.")
    private String password;
}

package org.example.serversprinteye.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ForgotPasswordRequest {
    @NotEmpty(message = "Podaj adres email")
    @Email(message = "Nieprawidłowy adres email")
    private String email;
}

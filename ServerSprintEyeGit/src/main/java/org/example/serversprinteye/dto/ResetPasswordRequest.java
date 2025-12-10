package org.example.serversprinteye.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ResetPasswordRequest {
    @NotEmpty(message = "Token jest wymagany")
    private String token;

    @NotEmpty(message = "Hasło jest wymagane")
    @Size(min = 6, message = "Hasło musi zawierać co najmniej 6 znaków")
    private String newPassword;

}

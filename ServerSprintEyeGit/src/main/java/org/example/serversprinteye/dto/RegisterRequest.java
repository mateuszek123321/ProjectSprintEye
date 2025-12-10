package org.example.serversprinteye.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.aspectj.bridge.IMessage;
import org.example.serversprinteye.domain.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class RegisterRequest {
    @NotEmpty(message = "Email jest wymagany")
    @Email(message = "Nieprawidłowy adres email")
    private String userEmail;

    //dd/mm/yyyy
    @Past(message = "Data urodzenia nie jest z przeszłości")
    private String birthDate;

    @NotEmpty(message = "Hasło jest wymagane")
    @Size(min = 6, message = "Hasło musi mieć co najmniej 6 znaków")
    private String password;

    private String gender;

    @NotEmpty(message = "Proszę podać nazwę użytkownika")
    @Size(min = 3, max = 20, message = "Nazwa użytkownika może zawierać maks. 20 znaków, min. 3")
    private String userName;
}

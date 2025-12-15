package org.example.serversprinteye.services;

import org.example.serversprinteye.domain.Gender;
import org.example.serversprinteye.domain.User;
import org.example.serversprinteye.dto.RegisterRequest;
import org.example.serversprinteye.repositories.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;
    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest(
                "testowy@gmail.com",
                "01/01/2002",
                "password123",
                "MALE",
                "testuser");
    }

    @Test
    @DisplayName("Rejestracja uzytkownika")
    void shouldRegisterUser() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        User expectedUser = new User()
                .setUserEmail("testowy@gmail.com")
                .setUserName("testuser")
                .setPassword("hashedPassword")
                .setGender(Gender.MALE);

        when(userRepo.save(any(User.class))).thenReturn(expectedUser);
        User result = authService.Register(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUserEmail()).isEqualTo("testowy@gmail.com");
        assertThat(result.getUsername()).isEqualTo("testowy@gmail.com");
        assertThat(result.getGender()).isEqualTo(Gender.MALE);

        verify(passwordEncoder).encode("password123");
        verify(userRepo).save(any(User.class));
    }

    @Test
    @DisplayName("haszowanie hasła przy rejestracji")
    void shouldEncodePassword() {
        String rawPassword = "password123";
        String hashedPassword = "hashedPassword";

        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = authService.Register(validRequest);

        assertThat(result.getPassword()).isEqualTo(hashedPassword);
        assertThat(result.getPassword()).isNotEqualTo(rawPassword);
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    @DisplayName("Wyjatek przy nieprawidlowej plci")
    void shouldThrowExceptionForInvalidGender() {
        RegisterRequest invalidRequest = new RegisterRequest(
                "testowy@example.com",
                "01/01/2002",
                "password123",
                "INVALID_GENDER",
                "testuser");
        assertThatThrownBy(() -> authService.Register(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nieprawidłowa płeć");
    }
}

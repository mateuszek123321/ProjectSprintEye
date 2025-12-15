package org.example.serversprinteye.services;

import lombok.RequiredArgsConstructor;
import org.example.serversprinteye.domain.User;
import org.example.serversprinteye.dto.LoginRequest;
import org.example.serversprinteye.dto.RegisterRequest;
import org.example.serversprinteye.repositories.UserRepo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor

public class AuthService {
    private final UserRepo userRepo;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public User Register(RegisterRequest input){
        org.example.serversprinteye.domain.Gender parsedGender = null;
        try{
            parsedGender = org.example.serversprinteye.domain.Gender.valueOf(input.getGender().toUpperCase());
        }catch (Exception e){
            throw new IllegalArgumentException("Nieprawidłowa płeć");
        }

        User user = new User()
        .setUserName(input.getUserName())
        .setUserEmail(input.getUserEmail())
        .setBirthDate(input.getBirthDate())
        .setGender(parsedGender)
        .setPassword(passwordEncoder.encode(input.getPassword()));

        return userRepo.save(user);
    }
    public User authenticate(LoginRequest input){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getIdentifier(),
                        input.getPassword()
                )
        );
        boolean isEmail = input.getIdentifier().contains("@");
        return (isEmail ? userRepo.findByUserEmail(input.getIdentifier()) : userRepo.findByUserName(input.getIdentifier()))
                .orElseThrow();
    }
}

package org.example.serversprinteye.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.serversprinteye.domain.User;
import org.example.serversprinteye.dto.*;
import org.example.serversprinteye.repositories.UserRepo;
import org.example.serversprinteye.services.AuthService;
import org.example.serversprinteye.services.EmailVerificationService;
import org.example.serversprinteye.services.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {
    private final JwtService jwtService;
    private final AuthService authService;
    private final UserRepo users;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody @Valid RegisterRequest registerRequest) {
        //check existing users
        Optional<User> existingUserEmail = users.findByUserEmail(registerRequest.getUserEmail());
        Optional<User> existingUserName = users.findByUserName(registerRequest.getUserName());

        if (existingUserEmail.isPresent()){
            throw new IllegalArgumentException("Email już zarejestrowany");
        }
        if (existingUserName.isPresent()){
            throw new IllegalArgumentException("Nazwa użytkownika już zajęta");
        }
        //register user
        User registeredUser = authService.Register(registerRequest);
        //generating token and send email
        String verifyToken = jwtService.generateToken(registeredUser);
        emailVerificationService.sendVerificationEmail(registeredUser.getUserEmail(), verifyToken);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticate(@RequestBody LoginRequest loginRequest){
        User authenticatedUser = authService.authenticate(loginRequest);

        String token = jwtService.generateToken(authenticatedUser);

        JwtResponse jwtResponse = new JwtResponse().setToken(token).setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest){
        Optional<User> userOptional = users.findByUserEmail(forgotPasswordRequest.getEmail());

        if (userOptional.isPresent()){
            User user = userOptional.get();
            String resetToken = jwtService.generateToken(user);
            emailVerificationService.sendForgotPasswordEmail(user.getUserEmail(), resetToken);
        }

        return ResponseEntity.ok("Jeśli podano dobry email, sprawdź skrzynkę odbiorczą");
    }
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token){
        try{
            String userEmail = jwtService.extractuserName(token);
            Optional<User> userOptional = users.findByUserEmail(userEmail);

            if (userOptional.isPresent() && jwtService.isTokenValid(token, userOptional.get())){
                User user = userOptional.get();
                user.setEmailVerified(true);
                users.save(user);
                return ResponseEntity.ok("Email zweryfikowany!");
            }else{
                return ResponseEntity.badRequest().body("Nieprawidłowy token");
            }
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Błąd podczas weryfikacji email" + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest){
        try{
            String userEmail = jwtService.extractuserName(resetPasswordRequest.getToken());
            Optional<User> userOptional = users.findByUserEmail(userEmail);

            if (userOptional.isPresent()){
                User user = userOptional.get();

                if (!jwtService.isTokenValid(resetPasswordRequest.getToken(), user)){
                    return ResponseEntity.badRequest().body("Token uwierzytelniania jest nieprawdiłowy lub wygasł");
                }

                //updating password
                user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
                users.save(user);

                return ResponseEntity.ok("Poprawnie zresetowano hasło");
            }else{
                return ResponseEntity.badRequest().body("Nie znaleziono użytkownika");
            }
        }catch(Exception e){
            return ResponseEntity.badRequest().body("Błąd podczas resetowania hasła." + e.getMessage());
        }
    }

    @GetMapping({"/check-email"})
    public ResponseEntity<Boolean> checkEmailAvailability(@RequestParam String email) {
        boolean emailExists = this.users.findByUserEmail(email).isPresent();
        return ResponseEntity.ok(emailExists);
    }

    @GetMapping({"/check-username"})
    public ResponseEntity<Boolean> checkUsernameAvailability(@RequestParam String username) {
        boolean usernameExists = this.users.findByUserName(username).isPresent();
        return ResponseEntity.ok(usernameExists);
    }
}

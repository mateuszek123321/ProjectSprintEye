package org.example.serversprinteye.services;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.example.serversprinteye.repositories.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Accessors(chain = true)
public class UserDetailServiceImplementation implements UserDetailsService {
    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException{
        boolean isEmail = identifier.contains("@");
        return (isEmail ? userRepo.findByUserEmail(identifier) : userRepo.findByUserName(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}

package com.utnGymGroup.gym_system.common.auth.credentials;

import com.utnGymGroup.gym_system.features.user.exceptions.UserNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final CredentialsRepository credentialsRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UserNotFoundException{
        return credentialsRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado. USERNAME: " + username));
    }
}

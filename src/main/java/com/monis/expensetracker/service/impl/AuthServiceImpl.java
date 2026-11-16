package com.monis.expensetracker.service.impl;

import com.monis.expensetracker.dto.AuthResponseDto;
import com.monis.expensetracker.dto.LoginRequestDto;
import com.monis.expensetracker.dto.RegisterRequestDto;
import com.monis.expensetracker.entity.Role;
import com.monis.expensetracker.entity.User;
import com.monis.expensetracker.exception.DuplicateResourceException;
import com.monis.expensetracker.exception.ResourceNotFoundException;
import com.monis.expensetracker.repository.UserRepository;
import com.monis.expensetracker.security.JwtTokenProvider;
import com.monis.expensetracker.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResponseDto register(RegisterRequestDto registerDto) {
        String normalizedEmail = registerDto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("User", "email", normalizedEmail);
        }

        User user = new User();
        user.setName(registerDto.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setRoles(Set.of(Role.ROLE_USER));

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateTokenFromUsername(savedUser.getEmail());
        Set<String> roleNames = savedUser.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new AuthResponseDto(token, savedUser.getId(), savedUser.getName(), savedUser.getEmail(), roleNames);
    }

    @Override
    public AuthResponseDto login(LoginRequestDto loginDto) {
        String normalizedEmail = loginDto.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, loginDto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", normalizedEmail));

        Set<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new AuthResponseDto(token, user.getId(), user.getName(), user.getEmail(), roleNames);
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResourceNotFoundException("No authenticated user in security context");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}

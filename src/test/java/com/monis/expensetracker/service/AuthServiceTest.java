package com.monis.expensetracker.service;

import com.monis.expensetracker.dto.AuthResponseDto;
import com.monis.expensetracker.dto.LoginRequestDto;
import com.monis.expensetracker.dto.RegisterRequestDto;
import com.monis.expensetracker.entity.Role;
import com.monis.expensetracker.entity.User;
import com.monis.expensetracker.exception.DuplicateResourceException;
import com.monis.expensetracker.repository.UserRepository;
import com.monis.expensetracker.security.JwtTokenProvider;
import com.monis.expensetracker.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Rohit Sharma", "rohit@test.com", "encodedPass", Set.of(Role.ROLE_USER));
    }

    @Test
    @DisplayName("Register new user successfully when email is unique")
    void shouldRegisterNewUser() {
        RegisterRequestDto request = new RegisterRequestDto("Rohit Sharma", "rohit@test.com", "PlainPass123");

        when(userRepository.existsByEmail("rohit@test.com")).thenReturn(false);
        when(passwordEncoder.encode("PlainPass123")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateTokenFromUsername("rohit@test.com")).thenReturn("mock-jwt-token");

        AuthResponseDto response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getEmail()).isEqualTo("rohit@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register throws DuplicateResourceException if email already taken")
    void shouldThrowOnDuplicateEmail() {
        RegisterRequestDto request = new RegisterRequestDto("Rohit Sharma", "rohit@test.com", "PlainPass123");

        when(userRepository.existsByEmail("rohit@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("rohit@test.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login successfully with valid credentials and return JWT")
    void shouldLoginSuccessfully() {
        LoginRequestDto loginRequest = new LoginRequestDto("rohit@test.com", "PlainPass123");
        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("mock-login-token");
        when(userRepository.findByEmail("rohit@test.com")).thenReturn(Optional.of(sampleUser));

        AuthResponseDto response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mock-login-token");
        assertThat(response.getEmail()).isEqualTo("rohit@test.com");
    }
}

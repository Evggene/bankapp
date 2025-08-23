package org.bea;

import org.bea.repository.User;
import org.bea.repository.UserRepository;
import org.bea.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void updatePassword_encodesAndSaves() {
        var id = UUID.randomUUID();
        var existing = User.builder()
                .id(id)
                .username("john")
                .password("{bcrypt}old")
                .enabled(true)
                .build();
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpass")).thenReturn("{bcrypt}newpass");

        userService.updatePassword("john", "newpass");

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo(id);
        assertThat(saved.getValue().getUsername()).isEqualTo("john");
        assertThat(saved.getValue().getPassword()).isEqualTo("{bcrypt}newpass");
    }

    @Test
    void updatePassword_userNotFound_throws() {
        when(userRepository.findByUsername("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updatePassword("nope", "x"))
                .isInstanceOf(UsernameNotFoundException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void editUserAccount_updatesAndSaves() {
        var existing = User.builder()
                .id(UUID.randomUUID())
                .username("mary")
                .password("{bcrypt}pwd")
                .name("Old Name")
                .birthdate(LocalDate.of(1990, 1, 1))
                .enabled(true)
                .build();
        when(userRepository.findByUsername("mary")).thenReturn(Optional.of(existing));

        userService.editUserAccount("mary", "New Name", LocalDate.of(2000, 2, 2));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("New Name");
        assertThat(saved.getValue().getBirthdate()).isEqualTo(LocalDate.of(2000, 2, 2));
    }

    @Test
    void editUserAccount_userNotFound_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.editUserAccount("ghost", "N", LocalDate.now()))
                .isInstanceOf(UsernameNotFoundException.class);
        verify(userRepository, never()).save(any());
    }
}


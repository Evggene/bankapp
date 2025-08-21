package org.bea;

import org.bea.controller.AccountsController;
import org.bea.repository.UserRepository;
import org.bea.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные на моках (web slice): поднимается только MVC слой.
 * Сервис и секьюрити-зависимости замоканы.
 */
@WithMockUser
@WebMvcTest(controllers = AccountsController.class)
class AccountsControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // Нужны, чтобы поднялся SecurityConfig
    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("POST /user/{login}/editPassword передаёт параметры в сервис")
    void editPassword_ok() throws Exception {
        mockMvc.perform(post("/user/john/editPassword")
                        .with(csrf())
                        .contentType("application/x-www-form-urlencoded")
                        .param("password", "newpass")
                        .param("confirmPassword", "newpass"))
                .andExpect(status().isOk());

        verify(userService).updatePassword("john", "newpass");
    }

    @Test
    @DisplayName("POST /user/{login}/editUserAccounts передаёт параметры в сервис")
    void editUserAccounts_ok() throws Exception {
        mockMvc.perform(post("/user/mary/editUserAccounts")
                        .with(csrf())
                        .contentType("application/x-www-form-urlencoded")
                        .param("name", "New Name")
                        .param("birthdate", "2000-02-02"))
                .andExpect(status().isOk());

        // birthdate парсится как LocalDate (формат ISO-8601 "yyyy-MM-dd")
        verify(userService).editUserAccount("mary", "New Name", java.time.LocalDate.parse("2000-02-02"));
    }
}

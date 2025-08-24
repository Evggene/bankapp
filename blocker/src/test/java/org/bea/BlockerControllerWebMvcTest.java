package org.bea;

import org.bea.config.SecurityConfig;
import org.bea.controller.BlockerController;
import org.bea.dto.BlockRequest;
import org.bea.dto.BlockResponse;
import org.bea.service.BlockerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BlockerController.class)
@Import(SecurityConfig.class)
@WithMockUser(authorities = "SCOPE_front_ui")
class BlockerControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BlockerService blockerService;

    @Test
    void check_returnsServiceResult() throws Exception {
        given(blockerService.decide(any(BlockRequest.class)))
                .willReturn(new BlockResponse(true, "allowed (attempt #1)", 1));

        String json = """
            { "login":"john", "action":"withdraw", "value":100, "currency":"USD" }
            """;

        mockMvc.perform(post("/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.allowed").value(true))
                .andExpect(jsonPath("$.reason").value("allowed (attempt #1)"))
                .andExpect(jsonPath("$.count").value(1));
    }
}

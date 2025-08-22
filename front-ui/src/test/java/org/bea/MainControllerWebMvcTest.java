package org.bea;

import org.bea.controller.MainController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MainController.class)
@AutoConfigureMockMvc(addFilters = false)
class MainControllerWebMvcTest {

    @Autowired MockMvc mockMvc;

    @Test
    void root_redirectsToMain() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/main"));
    }

    @Test
    @WithMockUser(username="mary")
    void main_setsLoginAttribute_andReturnsTemplate() throws Exception {
        mockMvc.perform(get("/main"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("login", equalTo("mary")))
                .andExpect(view().name("main"));
    }
}

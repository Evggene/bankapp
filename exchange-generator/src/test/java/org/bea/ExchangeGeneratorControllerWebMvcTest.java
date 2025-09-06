package org.bea;

import org.bea.controller.ExchangeGeneratorController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ExchangeGeneratorController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExchangeGeneratorControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getRates_returnsJsonArray_withCorsHeader() throws Exception {
        mockMvc.perform(get("/getRates").header("Origin","http://localhost:8080"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].value").exists());
    }
}

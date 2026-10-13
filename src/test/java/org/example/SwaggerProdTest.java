package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
class SwaggerProdTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void swaggerIsDisabledInProduction() throws Exception {
        for (String path : new String[]{"/v3/api-docs", "/v3/api-docs.yaml",
                "/v3/api-docs/swagger-config", "/swagger-ui.html", "/swagger-ui/index.html"}) {
            mockMvc.perform(get(path)).andExpect(status().isNotFound());
        }
    }
}

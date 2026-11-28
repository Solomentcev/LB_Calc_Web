package com.lb_calc_web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LbCalcWebApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void openApi_shouldExposeSuccessfulAndCommonErrorResponses() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/equipment'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/equipment'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/equipment'].post.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/equipment'].post.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/equipment'].post.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/equipment'].post.responses['405']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/equipment'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/equipment'].post.responses['415']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/equipment'].post.responses['500']").exists());
    }
}
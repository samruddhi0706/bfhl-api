package com.bajaj.bfhl.controller;

import com.bajaj.bfhl.dto.BfhlRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BfhlControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private BfhlRequest buildRequest(Object... elements) {
        BfhlRequest req = new BfhlRequest();
        req.setData(Arrays.asList(elements));
        return req;
    }

    @Test
    @DisplayName("POST /bfhl – returns 200 with correct body")
    void happyPath() throws Exception {
        BfhlRequest request = buildRequest("A", "1", "22", "$", "B", "7");

        mockMvc.perform(post("/bfhl")
                        .header("X-Request-Id", "REQ-1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "REQ-1001"))
                .andExpect(jsonPath("$.is_success").value(true))
                .andExpect(jsonPath("$.request_id").value("REQ-1001"))
                .andExpect(jsonPath("$.sum").value("30"))
                .andExpect(jsonPath("$.largest_number").value("22"))
                .andExpect(jsonPath("$.smallest_number").value("1"))
                .andExpect(jsonPath("$.alphabet_count").value(2))
                .andExpect(jsonPath("$.number_count").value(3))
                .andExpect(jsonPath("$.special_character_count").value(1))
                .andExpect(jsonPath("$.contains_duplicates").value(false))
                .andExpect(jsonPath("$.processing_time_ms").isNumber());
    }

    @Test
    @DisplayName("POST /bfhl – X-Request-Id header echoed in response")
    void requestIdEchoedInResponseHeader() throws Exception {
        BfhlRequest request = buildRequest("Z");

        mockMvc.perform(post("/bfhl")
                        .header("X-Request-Id", "CUSTOM-999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "CUSTOM-999"))
                .andExpect(jsonPath("$.request_id").value("CUSTOM-999"));
    }

    @Test
    @DisplayName("POST /bfhl – missing data field returns 400")
    void missingDataField() throws Exception {
        mockMvc.perform(post("/bfhl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.is_success").value(false));
    }

    @Test
    @DisplayName("POST /bfhl – malformed JSON returns 400")
    void malformedJson() throws Exception {
        mockMvc.perform(post("/bfhl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not valid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.is_success").value(false));
    }

    @Test
    @DisplayName("POST /bfhl – duplicates detected")
    void duplicatesDetected() throws Exception {
        BfhlRequest request = buildRequest("10", "10", "A", "A", "", null, "&", "5");

        mockMvc.perform(post("/bfhl")
                        .header("X-Request-Id", "REQ-1003")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contains_duplicates").value(true))
                .andExpect(jsonPath("$.unique_element_count").value(4));
    }

    @Test
    @DisplayName("POST /bfhl – negative and decimal numbers handled correctly")
    void negativesAndDecimals() throws Exception {
        BfhlRequest request = buildRequest("-10", "25.5", "-100.75", "B", "@", "5", "A9");

        mockMvc.perform(post("/bfhl")
                        .header("X-Request-Id", "REQ-1004")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum").value("-80.25"))
                .andExpect(jsonPath("$.largest_number").value("25.5"))
                .andExpect(jsonPath("$.smallest_number").value("-100.75"));
    }

    @Test
    @DisplayName("POST /bfhl – summary block is present")
    void summaryBlock() throws Exception {
        BfhlRequest request = buildRequest("A", null, "", "1");

        mockMvc.perform(post("/bfhl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.total_elements_received").value(4))
                .andExpect(jsonPath("$.summary.valid_elements_processed").value(2))
                .andExpect(jsonPath("$.summary.invalid_elements_ignored").value(2));
    }

    @Test
    @DisplayName("POST /bfhl – empty data array")
    void emptyDataArray() throws Exception {
        BfhlRequest request = new BfhlRequest();
        request.setData(Collections.emptyList());

        mockMvc.perform(post("/bfhl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_success").value(true))
                .andExpect(jsonPath("$.sum").value("0"))
                .andExpect(jsonPath("$.number_count").value(0));
    }

    @Test
    @DisplayName("POST /bfhl – missing X-Request-Id defaults gracefully")
    void missingRequestIdHeader() throws Exception {
        BfhlRequest request = buildRequest("1");

        mockMvc.perform(post("/bfhl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.request_id").value("UNKNOWN"));
    }
}

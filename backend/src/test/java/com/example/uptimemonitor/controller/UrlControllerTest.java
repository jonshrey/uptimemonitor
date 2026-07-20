package com.example.uptimemonitor.controller;

import com.example.uptimemonitor.model.Url;
import com.example.uptimemonitor.repository.UrlRepository;
import com.example.uptimemonitor.service.PingService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlController.class)
public class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean // Mocks the database layer so we don't need a real Postgres DB for this test
    private UrlRepository urlRepository;

    @MockitoBean // Mocks the ping service so we don't make real network calls
    private PingService pingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAllUrls_ReturnsOk() throws Exception {
        Url url = new Url();
        url.setId(1L);
        url.setAddress("https://example.com");
        url.setStatus("UP");
        url.setResponseTimeMs(150L);

        when(urlRepository.findAll()).thenReturn(List.of(url));

        mockMvc.perform(get("/api/urls"))
                .andExpect(status().isOk()) // Verifies HTTP 200 OK
                .andExpect(jsonPath("$[0].address").value("https://example.com"))
                .andExpect(jsonPath("$[0].status").value("UP"));
    }

    @Test
    public void testAddUrl_ReturnsCreated() throws Exception {
        Url savedUrl = new Url();
        savedUrl.setId(1L);
        savedUrl.setAddress("https://example.com");
        savedUrl.setStatus("PENDING");

        Url finalUrl = new Url();
        finalUrl.setId(1L);
        finalUrl.setAddress("https://example.com");
        finalUrl.setStatus("UP");
        finalUrl.setResponseTimeMs(100L);

        when(urlRepository.save(any(Url.class))).thenReturn(savedUrl);
        when(urlRepository.findById(1L)).thenReturn(Optional.of(finalUrl));

        String requestBody = objectMapper.writeValueAsString(Map.of("address", "https://example.com"));

        mockMvc.perform(post("/api/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated()) // Proves our ResponseEntity.created() logic works!
                .andExpect(jsonPath("$.address").value("https://example.com"))
                .andExpect(jsonPath("$.status").value("UP"));

        verify(pingService, times(1)).performPing(any(Url.class));
    }

    @Test
    public void testAddUrl_EmptyAddress_ReturnsBadRequest() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("address", ""));

        mockMvc.perform(post("/api/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest()); // Proves our validation logic works!
    }
}
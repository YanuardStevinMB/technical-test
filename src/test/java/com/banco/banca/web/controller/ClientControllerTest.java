package com.banco.banca.web.controller;

import com.banco.banca.domain.entity.Client;
import com.banco.banca.domain.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // If you have a custom @ControllerAdvice, add it here:
        // mockMvc = MockMvcBuilders.standaloneSetup(clientController)
        //         .setControllerAdvice(new GlobalExceptionHandler())
        //         .build();
        mockMvc = MockMvcBuilders.standaloneSetup(clientController).build();
    }

    @Test
    void postClient_Valid_Returns201() throws Exception {
        Client created = Client.builder()
                .id(UUID.randomUUID())
                .identificationType("CC")
                .identificationNumber("123")
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.now().minusYears(20))
                .build();

        when(clientService.create(any(Client.class))).thenReturn(created);

        Map<String, Object> body = new HashMap<>();
        body.put("firstName", "John");
        body.put("lastName", "Doe");
        body.put("identificationType", "CC");
        body.put("identificationNumber", "123");
        body.put("birthDate", "2005-01-01");

        String json = objectMapper.writeValueAsString(body);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void getClients_Returns200() throws Exception {
        when(clientService.search(any(), any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk());
    }
}

package com.banco.banca.web.controller;

import com.banco.banca.domain.entity.TransactionType;
import com.banco.banca.domain.service.TransactionService;
import com.banco.banca.web.dto.TransactionResponseDto;
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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
    }

    @Test
    void postDeposit_Returns201() throws Exception {
        TransactionResponseDto resp = new TransactionResponseDto();
        resp.setId(UUID.randomUUID());
        resp.setType(TransactionType.CONSIGNACION);

        when(transactionService.deposit(any(), any(), any(), any())).thenReturn(resp);

        Map<String, Object> body = new HashMap<>();
        body.put("destinationAccountId", UUID.randomUUID().toString());
        body.put("amount", 1000.00);
        body.put("description", "test");

        String json = objectMapper.writeValueAsString(body);

        mockMvc.perform(
                        post("/api/transactions/deposit")
                                .header("X-User", "tester") // if your controller reads this header, leave it here
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());
    }

    @Test
    void getTransactionsWithFilters_Returns200() throws Exception {
        when(transactionService.search(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/transactions")
                                .param("account", UUID.randomUUID().toString())
                                .param("fromDate", Instant.now().toString())
                )
                .andExpect(status().isOk());
    }
}

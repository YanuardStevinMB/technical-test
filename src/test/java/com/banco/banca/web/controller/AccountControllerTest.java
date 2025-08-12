package com.banco.banca.web.controller;

import com.banco.banca.domain.entity.Account;
import com.banco.banca.domain.entity.AccountStatus;
import com.banco.banca.domain.entity.AccountType;
import com.banco.banca.domain.service.AccountService;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // If you have a global @ControllerAdvice, uncomment and add the class:
        // mockMvc = MockMvcBuilders.standaloneSetup(accountController)
        //         .setControllerAdvice(new GlobalExceptionHandler())
        //         .build();
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
    }

    @Test
    void postAccount_Create_Returns201() throws Exception {
        Account account = Account.builder()
                .id(UUID.randomUUID())
                .accountType(AccountType.AHORROS)
                .accountNumber("5300000000")
                .status(AccountStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        when(accountService.create(any(), any(), any(), any())).thenReturn(account);

        Map<String, Object> body = new HashMap<>();
        body.put("clientId", UUID.randomUUID().toString());
        body.put("accountType", "AHORROS");
        body.put("gmfExempt", false);

        String json = objectMapper.writeValueAsString(body);

        mockMvc.perform(
                        post("/api/accounts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());
    }

    @Test
    void putAccount_UpdateStatus_Returns200() throws Exception {
        Account account = Account.builder()
                .id(UUID.randomUUID())
                .accountType(AccountType.AHORROS)
                .accountNumber("5300000000")
                .status(AccountStatus.INACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        when(accountService.updateStatus(any(UUID.class), any())).thenReturn(account);

        Map<String, Object> body = new HashMap<>();
        body.put("status", "INACTIVE");
        String json = objectMapper.writeValueAsString(body);

        mockMvc.perform(
                        put("/api/accounts/" + account.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk());
    }
}

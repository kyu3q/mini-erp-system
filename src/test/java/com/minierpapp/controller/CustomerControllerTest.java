package com.minierpapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minierpapp.config.TestSecurityConfig;
import com.minierpapp.model.common.Status;
import com.minierpapp.model.customer.dto.CustomerRequest;
import com.minierpapp.model.customer.dto.CustomerResponse;
import com.minierpapp.model.customer.mapper.CustomerMapper;
import com.minierpapp.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CustomerController.class)
@Import(TestSecurityConfig.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CustomerMapper customerMapper;

    private CustomerResponse testCustomerResponse;
    private CustomerRequest testCustomerRequest;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();

        testCustomerResponse = new CustomerResponse();
        testCustomerResponse.setId(1L);
        testCustomerResponse.setCustomerCode("CUST001");
        testCustomerResponse.setName("Test Customer");
        testCustomerResponse.setNameKana("テストカスタマー");
        testCustomerResponse.setStatus(Status.ACTIVE);
        testCustomerResponse.setCreatedAt(LocalDateTime.now());
        testCustomerResponse.setUpdatedAt(LocalDateTime.now());
        testCustomerResponse.setCreatedBy("system");
        testCustomerResponse.setUpdatedBy("system");

        testCustomerRequest = new CustomerRequest();
        testCustomerRequest.setCustomerCode("CUST001");
        testCustomerRequest.setName("Test Customer");
        testCustomerRequest.setNameKana("テストカスタマー");
        testCustomerRequest.setStatus(Status.ACTIVE);
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenGetCustomer_thenReturnCustomer() throws Exception {
        given(customerService.findByCustomerCode("CUST001"))
            .willReturn(testCustomerResponse);

        mockMvc.perform(get("/api/customers/code/CUST001")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerCode").value("CUST001"))
            .andExpect(jsonPath("$.name").value("Test Customer"))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenCreateCustomer_thenReturnCreatedCustomer() throws Exception {
        given(customerService.create(any(CustomerRequest.class)))
            .willReturn(testCustomerResponse);

        mockMvc.perform(post("/api/customers")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(testCustomerRequest)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerCode").value("CUST001"))
            .andExpect(jsonPath("$.name").value("Test Customer"))
            .andExpect(jsonPath("$.nameKana").value("テストカスタマー"));

        verify(customerService).create(any(CustomerRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenUpdateCustomer_thenReturnUpdatedCustomer() throws Exception {
        given(customerService.update(eq(1L), any(CustomerRequest.class)))
            .willReturn(testCustomerResponse);

        mockMvc.perform(put("/api/customers/{id}", 1L)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(objectMapper.writeValueAsString(testCustomerRequest)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerCode").value("CUST001"))
            .andExpect(jsonPath("$.name").value("Test Customer"));

        verify(customerService).update(eq(1L), any(CustomerRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenDeleteCustomer_thenReturn204() throws Exception {
        doNothing().when(customerService).delete(1L);

        mockMvc.perform(delete("/api/customers/{id}", 1L)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
            .andDo(print())
            .andExpect(status().isNoContent());

        verify(customerService).delete(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenSearchCustomers_thenReturnFilteredList() throws Exception {
        List<CustomerResponse> customers = Arrays.asList(testCustomerResponse);
        given(customerService.searchCustomers("Test"))
            .willReturn(customers);

        mockMvc.perform(get("/api/customers/search")
                .param("keyword", "Test")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].customerCode").value("CUST001"))
            .andExpect(jsonPath("$[0].name").value("Test Customer"));
    }

    @Test
    void whenUnauthorizedAccess_thenReturn401() throws Exception {
        mockMvc.perform(get("/api/customers")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenForbiddenAccess_thenReturn403() throws Exception {
        mockMvc.perform(post("/api/customers")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCustomerRequest)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }
}
package com.minierpapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CustomerController.class)
@WithMockUser
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
        testCustomerResponse = new CustomerResponse();
        testCustomerResponse.setId(1L);
        testCustomerResponse.setCustomerCode("CUST001");
        testCustomerResponse.setName("Test Customer");
        testCustomerResponse.setStatus(Status.ACTIVE);

        testCustomerRequest = new CustomerRequest();
        testCustomerRequest.setCustomerCode("CUST001");
        testCustomerRequest.setName("Test Customer");
        testCustomerRequest.setStatus(Status.ACTIVE);
    }

    @Test
    void whenGetCustomer_thenReturnCustomer() throws Exception {
        given(customerService.findByCustomerCode("CUST001"))
            .willReturn(testCustomerResponse);

        mockMvc.perform(get("/api/customers/code/CUST001")
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.customerCode").value("CUST001"))
            .andExpect(jsonPath("$.name").value("Test Customer"))
            .andExpect(jsonPath("$.status").value("有効")); // 日本語の表示名を期待
    }

    @Test
    void whenCreateCustomer_thenReturnCreatedCustomer() throws Exception {
        given(customerService.create(any(CustomerRequest.class)))
            .willReturn(testCustomerResponse);

        mockMvc.perform(post("/api/customers")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCustomerRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerCode").value("CUST001"))
            .andExpect(jsonPath("$.name").value("Test Customer"));
    }

    @Test
    void whenDeleteCustomer_thenReturn204() throws Exception {
        mockMvc.perform(delete("/api/customers/{id}", 1L)
                .with(SecurityMockMvcRequestPostProcessors.csrf()))
            .andExpect(status().isNoContent());
    }
}
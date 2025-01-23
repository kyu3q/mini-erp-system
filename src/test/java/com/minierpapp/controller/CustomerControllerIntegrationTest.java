package com.minierpapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minierpapp.model.common.Status;
import com.minierpapp.model.customer.dto.CustomerRequest;
import com.minierpapp.model.customer.dto.CustomerResponse;
import com.minierpapp.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerService customerService;

    @Test
    @WithMockUser(roles = "USER")
    void whenGetAllCustomers_thenReturnCustomersList() throws Exception {
        // when
        MvcResult result = mockMvc.perform(get("/api/customers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        List<CustomerResponse> customers = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, CustomerResponse.class)
        );
        assertThat(customers).isNotEmpty();
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenGetCustomerByCode_thenReturnCustomer() throws Exception {
        // when
        mockMvc.perform(get("/api/customers/code/{code}", "CUST001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerCode").value("CUST001"))
                .andExpect(jsonPath("$.name").value("テスト株式会社1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenCreateCustomer_thenReturnCreatedCustomer() throws Exception {
        // given
        CustomerRequest request = new CustomerRequest();
        request.setCustomerCode("CUST999");
        request.setName("新規テスト株式会社");
        request.setStatus(Status.ACTIVE);

        // when
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerCode").value("CUST999"))
                .andExpect(jsonPath("$.name").value("新規テスト株式会社"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenUpdateCustomer_thenReturnUpdatedCustomer() throws Exception {
        // given
        CustomerResponse existing = customerService.findByCustomerCode("CUST001");
        CustomerRequest request = new CustomerRequest();
        request.setCustomerCode("CUST001");
        request.setName("更新テスト株式会社");
        request.setStatus(Status.ACTIVE);

        // when
        mockMvc.perform(put("/api/customers/{id}", existing.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerCode").value("CUST001"))
                .andExpect(jsonPath("$.name").value("更新テスト株式会社"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenDeleteCustomer_thenReturn204() throws Exception {
        // given
        CustomerResponse existing = customerService.findByCustomerCode("CUST003");

        // when
        mockMvc.perform(delete("/api/customers/{id}", existing.getId()))
                .andExpect(status().isNoContent());

        // then
        mockMvc.perform(get("/api/customers/code/{code}", "CUST003"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenSearchCustomers_thenReturnFilteredCustomers() throws Exception {
        // when
        mockMvc.perform(get("/api/customers/search")
                .param("keyword", "テスト")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "name,asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").exists());
    }

    @Test
    void whenUnauthorizedAccess_thenReturn401() throws Exception {
        mockMvc.perform(get("/api/customers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenForbiddenAccess_thenReturn403() throws Exception {
        // given
        CustomerRequest request = new CustomerRequest();
        request.setCustomerCode("CUST888");
        request.setName("権限テスト株式会社");
        request.setStatus(Status.ACTIVE);

        // when
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenCreateInvalidCustomer_thenReturn400() throws Exception {
        // given
        CustomerRequest request = new CustomerRequest();
        request.setCustomerCode("");
        request.setName("");
        request.setStatus(Status.ACTIVE);

        // when
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenCreateDuplicateCustomer_thenReturn400() throws Exception {
        // given
        CustomerRequest request = new CustomerRequest();
        request.setCustomerCode("CUST001");
        request.setName("重複テスト株式会社");
        request.setStatus(Status.ACTIVE);

        // when
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("は既に使用されています")));
    }
}
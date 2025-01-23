package com.minierpapp.e2e;

import com.minierpapp.model.common.Status;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.customer.dto.CustomerRequest;
import com.minierpapp.model.customer.dto.CustomerResponse;
import com.minierpapp.repository.CustomerRepository;
import com.minierpapp.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class CustomerE2ETest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void testCustomerLifecycle() {
        // 1. 得意先の新規作成
        CustomerRequest createRequest = new CustomerRequest();
        createRequest.setCustomerCode("E2E001");
        createRequest.setName("E2Eテスト株式会社");
        createRequest.setNameKana("イーツーイーテストカブシキガイシャ");
        createRequest.setPostalCode("123-4567");
        createRequest.setAddress("東京都千代田区1-1-1");
        createRequest.setPhone("03-1234-5678");
        createRequest.setEmail("e2e@example.com");
        createRequest.setStatus(Status.ACTIVE);

        CustomerResponse created = customerService.create(createRequest);
        assertThat(created.getCustomerCode()).isEqualTo("E2E001");
        assertThat(created.getName()).isEqualTo("E2Eテスト株式会社");

        // 2. データベースに保存されていることを確認
        Optional<Customer> savedCustomer = customerRepository.findByCustomerCodeAndDeletedFalse("E2E001");
        assertThat(savedCustomer).isPresent();
        assertThat(savedCustomer.get().getName()).isEqualTo("E2Eテスト株式会社");

        // 3. 得意先情報の更新
        CustomerRequest updateRequest = new CustomerRequest();
        updateRequest.setCustomerCode("E2E001");
        updateRequest.setName("E2Eテスト株式会社（更新）");
        updateRequest.setStatus(Status.ACTIVE);

        CustomerResponse updated = customerService.update(savedCustomer.get().getId(), updateRequest);
        assertThat(updated.getName()).isEqualTo("E2Eテスト株式会社（更新）");

        // 4. 更新内容がデータベースに反映されていることを確認
        Optional<Customer> updatedCustomer = customerRepository.findByCustomerCodeAndDeletedFalse("E2E001");
        assertThat(updatedCustomer).isPresent();
        assertThat(updatedCustomer.get().getName()).isEqualTo("E2Eテスト株式会社（更新）");

        // 5. 検索機能のテスト
        Page<CustomerResponse> searchResult = customerService.search("E2E", 
            PageRequest.of(0, 10, Sort.by("name").ascending()));
        assertThat(searchResult.getContent()).hasSize(1);
        assertThat(searchResult.getContent().get(0).getCustomerCode()).isEqualTo("E2E001");

        // 6. 得意先の削除
        customerService.delete(updatedCustomer.get().getId());

        // 7. 削除後に検索できないことを確認
        assertThatThrownBy(() -> customerService.findByCustomerCode("E2E001"))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void testBulkOperations() {
        // 1. 一括登録のテスト
        List<CustomerRequest> bulkRequests = List.of(
            createCustomerRequest("BULK001", "一括テスト1"),
            createCustomerRequest("BULK002", "一括テスト2"),
            createCustomerRequest("BULK003", "一括テスト3")
        );

        List<CustomerResponse> bulkCreated = customerService.bulkCreate(bulkRequests);
        assertThat(bulkCreated).hasSize(3);

        // 2. 一括検索のテスト
        List<CustomerResponse> searchResults = customerService.searchCustomers("一括テスト");
        assertThat(searchResults).hasSize(3);

        // 3. データベースに保存されていることを確認
        List<Customer> savedCustomers = customerRepository.findByDeletedFalse();
        assertThat(savedCustomers).extracting(Customer::getCustomerCode)
            .contains("BULK001", "BULK002", "BULK003");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @Transactional
    void testErrorHandling() {
        // 1. 重複する顧客コードのテスト
        CustomerRequest duplicateRequest = createCustomerRequest("CUST001", "重複テスト");
        assertThatThrownBy(() -> customerService.create(duplicateRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("は既に使用されています");

        // 2. 不正なデータのテスト
        CustomerRequest invalidRequest = new CustomerRequest();
        assertThatThrownBy(() -> customerService.create(invalidRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("得意先コードを入力してください");

        // 3. 存在しないIDの更新テスト
        CustomerRequest updateRequest = createCustomerRequest("UPDATE001", "更新テスト");
        assertThatThrownBy(() -> customerService.update(999L, updateRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("not found");

        // 4. 存在しないIDの削除テスト
        assertThatThrownBy(() -> customerService.delete(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("not found");
    }

    private CustomerRequest createCustomerRequest(String code, String name) {
        CustomerRequest request = new CustomerRequest();
        request.setCustomerCode(code);
        request.setName(name);
        request.setStatus(Status.ACTIVE);
        return request;
    }
}
package com.minierpapp.config;

import com.minierpapp.model.common.Status;
import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.order.Order;
import com.minierpapp.model.order.OrderDetail;
import com.minierpapp.model.order.OrderStatus;
import com.minierpapp.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
@Profile("dev")
public class TestDataGenerator {

    private final Random random = new Random();

    @Bean
    public CommandLineRunner generateTestData(
            CustomerRepository customerRepository,
            ItemRepository itemRepository,
            OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository) {
        return args -> {
            // 追加の得意先データを作成
            List<Customer> additionalCustomers = new ArrayList<>();
            additionalCustomers.add(createCustomer("C003", "株式会社テックソリューション", "150-0001", "東京都渋谷区神宮前1-1-1"));
            additionalCustomers.add(createCustomer("C004", "グローバルIT株式会社", "220-0011", "横浜市西区高島2-2-2"));
            additionalCustomers.add(createCustomer("C005", "株式会社デジタルプロ", "460-0008", "名古屋市中区栄3-3-3"));
            customerRepository.saveAll(additionalCustomers);

            // 既存の得意先と商品を取得
            List<Customer> allCustomers = customerRepository.findAll();
            List<Item> allItems = itemRepository.findAll();

            if (!allCustomers.isEmpty() && !allItems.isEmpty()) {
                // 20件の受注データを作成
                for (int i = 1; i <= 20; i++) {
                    Order order = createOrder(i, allCustomers);
                    order = orderRepository.save(order);

                    // 1-3件の受注明細を作成
                    int detailCount = random.nextInt(3) + 1;
                    List<OrderDetail> details = new ArrayList<>();
                    for (int j = 1; j <= detailCount; j++) {
                        OrderDetail detail = createOrderDetail(j, order, allItems);
                        details.add(detail);
                    }
                    orderDetailRepository.saveAll(details);

                    // 合計金額を更新
                    BigDecimal totalAmount = details.stream()
                            .map(OrderDetail::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    order.setTotalAmount(totalAmount);
                    order.setTaxAmount(totalAmount.multiply(new BigDecimal("0.1")));
                    orderRepository.save(order);
                }
            }
        };
    }

    private Customer createCustomer(String code, String name, String postalCode, String address) {
        Customer customer = new Customer();
        customer.setCustomerCode(code);
        customer.setName(name);
        customer.setNameKana(name);
        customer.setPostalCode(postalCode);
        customer.setAddress(address);
        customer.setStatus(Status.ACTIVE);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setCreatedBy("system");
        return customer;
    }

    private Order createOrder(int index, List<Customer> customers) {
        Order order = new Order();
        order.setOrderNumber(String.format("ORD-%s-%03d", 
                LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM")), 
                index));
        order.setOrderDate(LocalDate.now().minusDays(random.nextInt(30)));
        order.setCustomer(customers.get(random.nextInt(customers.size())));
        order.setDeliveryDate(order.getOrderDate().plusDays(random.nextInt(14) + 1));
        order.setStatus(OrderStatus.values()[random.nextInt(OrderStatus.values().length)]);
        order.setCreatedAt(LocalDateTime.now());
        order.setCreatedBy("system");
        return order;
    }

    private OrderDetail createOrderDetail(int lineNumber, Order order, List<Item> items) {
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setLineNumber(lineNumber);
        Item selectedItem = items.get(random.nextInt(items.size()));
        detail.setItem(selectedItem);
        detail.setItemName(selectedItem.getItemName());
        detail.setQuantity(random.nextInt(5) + 1);
        detail.setUnitPrice(new BigDecimal(random.nextInt(50000) + 10000));
        detail.setAmount(detail.getUnitPrice().multiply(new BigDecimal(detail.getQuantity())));
        detail.setDeliveryDate(order.getDeliveryDate());
        detail.setCreatedAt(LocalDateTime.now());
        detail.setCreatedBy("system");
        return detail;
    }
}
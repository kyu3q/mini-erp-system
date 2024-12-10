package com.minierpapp.controller;

import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.order.OrderStatus;
import com.minierpapp.model.order.dto.OrderDto;
import com.minierpapp.model.order.dto.OrderRequest;
import com.minierpapp.model.product.Product;
import com.minierpapp.model.warehouse.Warehouse;
import com.minierpapp.service.CustomerService;
import com.minierpapp.service.OrderService;
import com.minierpapp.service.ProductService;
import com.minierpapp.service.WarehouseService;
import com.minierpapp.util.ExcelHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderWebController {
    private final OrderService orderService;
    private final CustomerService customerService;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final ExcelHelper excelHelper;

    @GetMapping
    public String list(Model model) {
        List<OrderDto> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        return "order/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        List<Customer> customers = customerService.findAllActive();
        List<Product> products = productService.findAllActive();
        List<Warehouse> warehouses = warehouseService.findAllActive();
        model.addAttribute("customers", customers);
        model.addAttribute("products", products);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("statuses", OrderStatus.values());
        return "order/edit";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        OrderDto order = orderService.findById(id);
        List<Customer> customers = customerService.findAllActive();
        List<Product> products = productService.findAllActive();
        List<Warehouse> warehouses = warehouseService.findAllActive();
        model.addAttribute("order", order);
        model.addAttribute("customers", customers);
        model.addAttribute("products", products);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("statuses", OrderStatus.values());
        return "order/edit";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute OrderRequest request, RedirectAttributes redirectAttributes) {
        try {
            if (request.getId() == null) {
                orderService.create(request);
                redirectAttributes.addFlashAttribute("successMessage", "受注を登録しました。");
            } else {
                orderService.update(request.getId(), request);
                redirectAttributes.addFlashAttribute("successMessage", "受注を更新しました。");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました: " + e.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "受注を削除しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました: " + e.getMessage());
        }
        return "redirect:/orders";
    }

    @GetMapping("/export")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<OrderDto> orders = orderService.findAll();
        excelHelper.exportOrdersToExcel(response, orders);
    }

    @PostMapping("/import")
    public String importFromExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            List<OrderRequest> orders = excelHelper.importOrdersFromExcel(file);
            for (OrderRequest order : orders) {
                orderService.create(order);
            }
            redirectAttributes.addFlashAttribute("successMessage", "受注データをインポートしました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "インポートに失敗しました: " + e.getMessage());
        }
        return "redirect:/orders";
    }
}
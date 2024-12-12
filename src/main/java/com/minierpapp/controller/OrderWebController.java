package com.minierpapp.controller;

import com.minierpapp.model.customer.Customer;
import com.minierpapp.model.order.OrderStatus;
import com.minierpapp.model.order.dto.OrderDto;
import com.minierpapp.model.order.dto.OrderRequest;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.warehouse.Warehouse;
import com.minierpapp.service.CustomerService;
import com.minierpapp.service.OrderService;
import com.minierpapp.service.ItemService;
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
    private final ItemService itemService;
    private final WarehouseService warehouseService;
    private final ExcelHelper excelHelper;

    @GetMapping
    public String list(@RequestParam(required = false) String orderNumber,
                      @RequestParam(required = false) String orderDateFrom,
                      @RequestParam(required = false) String orderDateTo,
                      @RequestParam(required = false) Long customerId,
                      @RequestParam(required = false) Long itemId,
                      @RequestParam(required = false) OrderStatus status,
                      Model model) {
        List<OrderDto> orders = orderService.search(orderNumber, orderDateFrom, orderDateTo,
                customerId, itemId, status);
        List<Customer> customers = customerService.findAllActive();
        List<Item> items = itemService.findAllActive();

        model.addAttribute("orders", orders);
        model.addAttribute("customers", customers);
        model.addAttribute("items", items);
        model.addAttribute("statuses", OrderStatus.values());
        return "order/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        prepareFormData(model);
        return "order/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        OrderDto order = orderService.findById(id);
        model.addAttribute("order", order);
        prepareFormData(model);
        return "order/form";
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

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "受注を削除しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました: " + e.getMessage());
        }
        return "redirect:/orders";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam OrderStatus status, RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "受注ステータスを更新しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "ステータス更新中にエラーが発生しました：" + e.getMessage());
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

    private void prepareFormData(Model model) {
        List<Customer> customers = customerService.findAllActive();
        List<Item> items = itemService.findAllActive();
        List<Warehouse> warehouses = warehouseService.findAllActive();
        model.addAttribute("customers", customers);
        model.addAttribute("items", items);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("statuses", OrderStatus.values());
    }
}
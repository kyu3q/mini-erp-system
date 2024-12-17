package com.minierpapp.controller;

import com.minierpapp.controller.base.BaseWebController;
import com.minierpapp.model.item.Item;
import com.minierpapp.model.item.dto.ItemDto;
import com.minierpapp.model.item.dto.ItemRequest;
import com.minierpapp.model.item.dto.ItemResponse;
import com.minierpapp.model.item.mapper.ItemMapper;
import com.minierpapp.service.ItemService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/items")
public class ItemWebController extends BaseWebController<Item, ItemDto, ItemRequest, ItemResponse> {

    private final ItemService itemService;

    public ItemWebController(ItemMapper mapper, MessageSource messageSource, ItemService itemService) {
        super(mapper, messageSource, "items", "item");
        this.itemService = itemService;
    }

    @Override
    @GetMapping
    public String list(
            @RequestParam(required = false) String searchParam1,
            @RequestParam(required = false) String searchParam2,
            Model model) {
        String itemCode = searchParam1;
        String itemName = searchParam2;
        if ((itemCode != null && !itemCode.trim().isEmpty()) ||
            (itemName != null && !itemName.trim().isEmpty())) {
            model.addAttribute("items", itemService.search(itemCode, itemName));
            model.addAttribute("itemCode", itemCode);
            model.addAttribute("itemName", itemName);
        } else {
            model.addAttribute("items", findAll());
        }
        return getListTemplate();
    }

    @Override
    protected List<ItemResponse> findAll() {
        return itemService.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    protected ItemRequest createNewRequest() {
        return new ItemRequest();
    }

    @Override
    protected ItemRequest findById(Long id) {
        Item item = itemService.findById(id);
        ItemRequest request = new ItemRequest();
        request.setId(id);
        request.setItemCode(item.getItemCode());
        request.setItemName(item.getItemName());
        request.setDescription(item.getDescription());
        request.setUnit(item.getUnit());
        request.setStatus(item.getStatus());
        request.setMinimumStock(item.getMinimumStock());
        request.setMaximumStock(item.getMaximumStock());
        request.setReorderPoint(item.getReorderPoint());
        return request;
    }

    @Override
    protected void createEntity(ItemRequest request) {
        itemService.create(request);
    }

    @Override
    protected void updateEntity(Long id, ItemRequest request) {
        itemService.update(id, request);
    }

    @Override
    protected void deleteEntity(Long id) {
        itemService.delete(id);
    }

    @Override
    protected void handleError(BindingResult result, Exception e) {
        if (e.getMessage().contains("品目コード")) {
            result.rejectValue("itemCode", "error.itemCode", e.getMessage());
        } else if (e.getMessage().contains("最小在庫数")) {
            result.rejectValue("minimumStock", "error.minimumStock", e.getMessage());
        } else if (e.getMessage().contains("最大在庫数")) {
            result.rejectValue("maximumStock", "error.maximumStock", e.getMessage());
        } else if (e.getMessage().contains("発注点")) {
            result.rejectValue("reorderPoint", "error.reorderPoint", e.getMessage());
        } else {
            super.handleError(result, e);
        }
    }
}
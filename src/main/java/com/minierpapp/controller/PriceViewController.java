package com.minierpapp.controller;

import com.minierpapp.model.price.ConditionType;
import com.minierpapp.model.price.PriceType;
import com.minierpapp.model.price.dto.PriceRequest;
import com.minierpapp.service.PriceService;
import com.minierpapp.service.PriceExcelImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/prices")
@RequiredArgsConstructor
public class PriceViewController {
    private final PriceService priceService;
    private final PriceExcelImportService priceExcelImportService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("prices", priceService.findAll());
        return "price/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("priceRequest", new PriceRequest());
        model.addAttribute("priceTypes", PriceType.values());
        model.addAttribute("conditionTypes", ConditionType.values());
        return "price/form";
    }

    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute PriceRequest priceRequest,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("priceTypes", PriceType.values());
            model.addAttribute("conditionTypes", ConditionType.values());
            return "price/form";
        }

        priceService.createPrice(priceRequest);
        redirectAttributes.addFlashAttribute("message", "単価マスタを登録しました。");
        return "redirect:/prices";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("priceRequest", priceService.getPrice(id));
        model.addAttribute("priceTypes", PriceType.values());
        model.addAttribute("conditionTypes", ConditionType.values());
        return "price/form";
    }

    @PostMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            @Valid @ModelAttribute PriceRequest priceRequest,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("priceTypes", PriceType.values());
            model.addAttribute("conditionTypes", ConditionType.values());
            return "price/form";
        }

        priceService.updatePrice(id, priceRequest);
        redirectAttributes.addFlashAttribute("message", "単価マスタを更新しました。");
        return "redirect:/prices";
    }

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        priceService.deletePrice(id);
        redirectAttributes.addFlashAttribute("message", "単価マスタを削除しました。");
        return "redirect:/prices";
    }

    @PostMapping("/duplicate/{id}")
    public String duplicate(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            priceService.duplicatePrice(id);
            redirectAttributes.addFlashAttribute("message", "単価マスタを複製しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "単価マスタの複製に失敗しました: " + e.getMessage());
        }
        return "redirect:/prices";
    }

    @PostMapping("/import")
    public String importPrices(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            priceExcelImportService.importPrices(file);
            redirectAttributes.addFlashAttribute("message", "単価マスタを一括登録しました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "単価マスタの一括登録に失敗しました: " + e.getMessage());
        }
        return "redirect:/prices";
    }
}
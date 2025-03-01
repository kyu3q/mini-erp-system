
package com.minierpapp.controller.base;

import com.minierpapp.model.base.BaseEntity;
import com.minierpapp.model.base.BaseMapper;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

public abstract class BaseWebController<E extends BaseEntity, D, Q, R> {
    protected final BaseMapper<E, D, Q, R> mapper;
    protected final MessageSource messageSource;
    protected final String viewPath;
    protected final String entityName;

    protected BaseWebController(BaseMapper<E, D, Q, R> mapper, MessageSource messageSource, String viewPath, String entityName) {
        this.mapper = mapper;
        this.messageSource = messageSource;
        this.viewPath = viewPath;
        this.entityName = entityName;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String searchParam1,
            @RequestParam(required = false) String searchParam2,
            Model model) {
        model.addAttribute(getListAttributeName(), findAll());
        return getListTemplate();
    }

    @GetMapping("/new")
    public String newEntity(Model model) {
        prepareForm(model, createNewRequest());
        return getFormTemplate();
    }

    @PostMapping
    public String create(@Valid @ModelAttribute(binding = true) Q request,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return handleValidationError(request, result, model);
        }

        try {
            createEntity(request);
            addSuccessMessage(redirectAttributes, getCreateSuccessMessage());
            return getRedirectToList();
        } catch (Exception e) {
            handleError(result, e);
            return handleValidationError(request, result, model);
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        prepareForm(model, mapper.responseToRequest(findById(id)));
        return getFormTemplate();
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute(binding = true) Q request,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return handleValidationError(request, result, model);
        }

        try {
            updateEntity(id, request);
            addSuccessMessage(redirectAttributes, getUpdateSuccessMessage());
            return getRedirectToList();
        } catch (Exception e) {
            handleError(result, e);
            return handleValidationError(request, result, model);
        }
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            deleteEntity(id);
            addSuccessMessage(redirectAttributes, getDeleteSuccessMessage());
        } catch (Exception e) {
            addErrorMessage(redirectAttributes, e.getMessage());
        }
        return getRedirectToList();
    }

    protected String getListTemplate() {
        return viewPath + "/list";
    }

    protected String getFormTemplate() {
        return viewPath + "/form";
    }

    protected String getRedirectToList() {
        return "redirect:/" + viewPath;
    }

    protected void addSuccessMessage(RedirectAttributes redirectAttributes, String messageKey) {
        String entityDisplayName = messageSource.getMessage(entityName + ".name", null, LocaleContextHolder.getLocale());
        String message = messageSource.getMessage(messageKey, new Object[]{entityDisplayName}, LocaleContextHolder.getLocale());
        redirectAttributes.addFlashAttribute("message", message);
        redirectAttributes.addFlashAttribute("messageType", "success");
    }

    protected void addErrorMessage(RedirectAttributes redirectAttributes, String messageKey, Object... args) {
        String message = messageSource.getMessage(messageKey, args, LocaleContextHolder.getLocale());
        redirectAttributes.addFlashAttribute("message", message);
        redirectAttributes.addFlashAttribute("messageType", "danger");
    }

    protected String handleValidationError(Q request, BindingResult result, Model model) {
        model.addAttribute(getModelAttributeName(), request);
        return getFormTemplate();
    }

    protected String getModelAttributeName() {
        return entityName.substring(0, 1).toLowerCase() + entityName.substring(1) + "Request";
    }

    protected String getListAttributeName() {
        return viewPath;
    }

    protected void prepareForm(Model model, Q request) {
        model.addAttribute(getModelAttributeName(), request);
    }

    protected void handleError(BindingResult result, Exception e) {
        result.reject("error.global", e.getMessage());
    }

    protected String getCreateSuccessMessage() {
        return "common.created";
    }

    protected String getUpdateSuccessMessage() {
        return "common.updated";
    }

    protected String getDeleteSuccessMessage() {
        return "common.deleted";
    }

    protected abstract List<R> findAll();
    protected abstract Q createNewRequest();
    protected abstract R findById(Long id);
    protected abstract void createEntity(Q request);
    protected abstract void updateEntity(Long id, Q request);
    protected abstract void deleteEntity(Long id);
}
package com.minierpapp.controller.base;

import com.minierpapp.model.base.BaseEntity;
import com.minierpapp.model.base.BaseMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestMapping;

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
            Model model,
            HttpServletRequest request) {
        
        // 検索条件の準備
        prepareSearchCriteria(model, request);
        
        // 検索結果がモデルに既に追加されている場合は、findAll()を呼び出さない
        if (!model.containsAttribute(getListAttributeName())) {
            // 検索結果の取得
            List<R> entities = findAll();
            model.addAttribute(getListAttributeName(), entities);
        }
        
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
        // バリデーションエラーがある場合は、フォーム画面に戻る
        if (result.hasErrors()) {
            return handleValidationError(request, result, model);
        }
        
        try {
            // Long createdId = createEntityAndGetId(request);
            // setRequestId(request, createdId);
            createEntity(request);
            addSuccessMessage(redirectAttributes, getCreateSuccessMessage());
            return "redirect:" + getSuccessCreateRedirectUrl(request);
        } catch (Exception e) {
            addErrorMessage(model, e);
            prepareForm(model, request);
            return getFormTemplate();
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
            return getRedirectToEdit(id);
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
        return "redirect:" + getBaseUrl();
    }

    protected String getRedirectToEdit(Long id) {
        return "redirect:" + getBaseUrl() + "/" + id + "/edit";
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

    protected void addErrorMessage(Model model, Exception e) {
        String message = e.getMessage();
        model.addAttribute("message", message);
        model.addAttribute("messageType", "danger");
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

    /**
     * エンティティ作成後のリダイレクト先URLを取得する
     * デフォルトでは作成したエンティティの編集画面に遷移する
     */
    protected String getSuccessCreateRedirectUrl(Q request) {
        try {
            java.lang.reflect.Method getIdMethod = request.getClass().getMethod("getId");
            Long id = (Long) getIdMethod.invoke(request);
            if (id != null) {
                return getBaseUrl() + "/" + id + "/edit";
            }
        } catch (Exception e) {
            // getId()メソッドがない場合は無視
        }
        return getBaseUrl();
    }

    protected String getBaseUrl() {
        // サブクラスの@RequestMappingアノテーションを取得
        RequestMapping requestMapping = this.getClass().getAnnotation(RequestMapping.class);
        if (requestMapping != null && requestMapping.value().length > 0) {
            return requestMapping.value()[0];  // アノテーションの値を返す
        }
        // フォールバック: viewPathを使用
        return "/" + viewPath;
    }

    // protected abstract Long createEntityAndGetId(Q request);
    // protected abstract void setRequestId(Q request, Long id);

    protected void prepareSearchCriteria(Model model, HttpServletRequest request) {
        // リクエストパラメータから検索条件を作成
        // 子クラスでオーバーライドして実装
    }
}
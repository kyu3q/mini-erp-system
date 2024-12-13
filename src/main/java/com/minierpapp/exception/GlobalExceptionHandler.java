package com.minierpapp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model) {
        logger.error("Unexpected error occurred", e);
        
        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("error", "Internal Server Error");
        model.addAttribute("message", e.getMessage());
        model.addAttribute("trace", getStackTrace(e));
        
        return "error";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException(ResourceNotFoundException e, Model model) {
        logger.error("Resource not found", e);
        
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        model.addAttribute("error", "Resource Not Found");
        model.addAttribute("message", e.getMessage());
        
        return "error";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFoundException(NoResourceFoundException e, Model model) {
        // 静的リソースが見つからない場合は、WARNレベルでログを出力
        logger.warn("Static resource not found: {}", e.getMessage());
        
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        model.addAttribute("error", "Resource Not Found");
        model.addAttribute("message", "The requested resource was not found");
        
        return "error";
    }

    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
package com.minierpapp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Object handleException(Exception e, Model model) {
        logger.error("Unexpected error occurred", e);
        
        if (isApiRequest()) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            body.put("error", "Internal Server Error");
            body.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }

        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("error", "Internal Server Error");
        model.addAttribute("message", e.getMessage());
        model.addAttribute("trace", getStackTrace(e));
        
        return "error";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Object handleResourceNotFoundException(ResourceNotFoundException e, Model model) {
        logger.error("Resource not found", e);
        
        if (isApiRequest()) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.NOT_FOUND.value());
            body.put("error", "Resource Not Found");
            body.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        model.addAttribute("error", "Resource Not Found");
        model.addAttribute("message", e.getMessage());
        
        return "error";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Object handleNoResourceFoundException(NoResourceFoundException e, Model model) {
        // 静的リソースが見つからない場合は、WARNレベルでログを出力
        logger.warn("Static resource not found: {}", e.getMessage());
        
        if (isApiRequest()) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.NOT_FOUND.value());
            body.put("error", "Resource Not Found");
            body.put("message", "The requested resource was not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }

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

    private boolean isApiRequest() {
        String requestURI = org.springframework.web.context.request.RequestContextHolder
                .currentRequestAttributes()
                .getAttribute("org.springframework.web.servlet.HandlerMapping.pathWithinHandlerMapping", 0)
                .toString();
        return requestURI.startsWith("/api/");
    }
}
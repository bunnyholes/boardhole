package dev.xiyo.bunnyholes.boardhole.user.presentation.view;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import dev.xiyo.bunnyholes.boardhole.shared.exception.ResourceNotFoundException;

@TestComponent
@ControllerAdvice(annotations = Controller.class)
public class TestViewExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return "error/404";
    }
}
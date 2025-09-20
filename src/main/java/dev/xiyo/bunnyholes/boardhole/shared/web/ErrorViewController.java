package dev.xiyo.bunnyholes.boardhole.shared.web;

import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import dev.xiyo.bunnyholes.boardhole.shared.util.MessageUtils;

@Controller
@RequestMapping("/error")
public class ErrorViewController {

    @GetMapping("/403")
    public String forbidden(Model model, HttpServletRequest request) {
        if (!model.containsAttribute("error")) {
            model.addAttribute("error", MessageUtils.get("error.access.denied"));
        }
        if (!model.containsAttribute("path")) {
            model.addAttribute("path", request.getRequestURI());
        }
        if (!model.containsAttribute("timestamp")) {
            model.addAttribute("timestamp", Instant.now());
        }
        return "error/403";
    }
}

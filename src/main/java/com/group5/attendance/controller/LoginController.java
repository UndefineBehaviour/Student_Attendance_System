package com.group5.attendance.controller;

import com.group5.attendance.model.User;
import com.group5.attendance.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletRequest request,
                        Model model) {
        Optional<User> user = userService.authenticate(username.trim(), password);
        if (user.isEmpty()) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
        request.getSession(true);
        request.changeSessionId();                       // prevents session fixation
        HttpSession session = request.getSession();
        session.setAttribute("user", user.get());
        String back = (String) session.getAttribute("returnTo");     // set by AuthInterceptor
        session.removeAttribute("returnTo");
        if (back != null && back.startsWith("/") && !back.startsWith("//") && !back.contains("\\")
                && !back.startsWith("/login") && !back.startsWith("/logout")) {
            return "redirect:" + back;                   // e.g. /scan?s=12&t=...
        }
        return "redirect:/dashboard";                    // POST then redirect
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
package de.hiwiapp.hiwiapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {

    // Die Seite vor dem Login
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // Die Seite direkt nach dem Login
    @GetMapping("/home")
    public String home() {
        // Wir schicken einfach alle auf die index-Seite zurück,
        // aber diesmal sind sie eingeloggt!
        return "index";
    }

    // Mapping für die Login-Seite selbst
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister() {
        // TODO: User speichern
        return "redirect:/home";
    }


}
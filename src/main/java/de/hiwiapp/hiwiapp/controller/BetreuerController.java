package de.hiwiapp.hiwiapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BetreuerController {
    @GetMapping("/betreuer")
    public String betreuerDashboard() {
        return "betreuer/dashboard";
    }
}
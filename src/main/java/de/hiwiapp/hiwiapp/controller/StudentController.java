package de.hiwiapp.hiwiapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentController {
    @GetMapping("/student")
    public String studentDashboard() {
        return "student/dashboard";
    }
}
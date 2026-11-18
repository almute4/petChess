package org.chess.mainservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    @GetMapping({"/login", "/register", "/lobby", "/settings", "/history", "/game/{id}"})
    public String index() {
        return "forward:/index.html";
    }
}

package com.babydiary.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {
    @RequestMapping({"/login", "/register", "/profile", "/posts",
            "/diary/**", "/post/**", "/moment/**",
            "/moments", "/ai-doctor", "/moment/create", "/post/create"})
    public String forward() {
        return "forward:/index.html";
    }
}

package com.hopu.bigdata.controller;

import com.hopu.bigdata.model.Userinfo;
import com.hopu.bigdata.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class IndexController {

    @Autowired
    private UserService userService;

    @GetMapping({"/", "/index", "/home"})
    public String index(@AuthenticationPrincipal Principal principal, Model model){
        return "index";
    }
}

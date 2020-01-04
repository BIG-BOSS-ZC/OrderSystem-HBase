package com.hopu.bigdata.controller;

import com.hopu.bigdata.mapper.UserinfoMapper;
import com.hopu.bigdata.model.Userinfo;
import com.hopu.bigdata.service.UserService;
import org.apache.zookeeper.server.SessionTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @GetMapping("")
    public void user(@AuthenticationPrincipal Principal principal, Model model){
        Userinfo user1 = userService.getUserByName(principal.getName());
        model.addAttribute("username", principal.getName());
    }

    @RequestMapping("index")
    public String index(@AuthenticationPrincipal Principal principal, Model model) {
        Userinfo user1 = userService.getUserByName(principal.getName());
        model.addAttribute("user", user1);
        model.addAttribute("modify", true);
        return "registry";
    }
}

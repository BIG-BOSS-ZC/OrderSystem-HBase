package com.hopu.bigdata.controller;

import com.hopu.bigdata.model.Userinfo;
import com.hopu.bigdata.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class LoginController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/login-error")
    public String loginError() {
        return "login-error";
    }

    @GetMapping("/registry")
    public String registry(Model model) {
        Userinfo user = new Userinfo();
        user.setSex("男");
        model.addAttribute("user", user);
        return "registry";
    }

    @PostMapping("/registry")
    public String registry(@ModelAttribute("user") Userinfo user, Model model,
                           @RequestParam(required=false) boolean modify) {

        model.addAttribute("modify", modify);

        // 用户已存在
        if (!modify && userService.getUserByName(user.getLoginname()) != null) {
            model.addAttribute("error", true);
            model.addAttribute("error_msg", "用户 '" + user.getLoginname() + "'已存在！");
            return "registry";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (!modify) {
            userService.save(user);
        } else {
            userService.updateById(user);
            model.addAttribute("result", "用户修改成功！");
            return "registry";
        }

        return "login";
    }
}

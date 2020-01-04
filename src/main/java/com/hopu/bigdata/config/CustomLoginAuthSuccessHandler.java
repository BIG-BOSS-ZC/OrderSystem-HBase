package com.hopu.bigdata.config;

import com.hopu.bigdata.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class CustomLoginAuthSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws ServletException, IOException {

        User user = (User) authentication.getPrincipal();
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute("userid", userService.getUserByName(user.getUsername()).getId());

        super.onAuthenticationSuccess(httpServletRequest,httpServletResponse,authentication);
    }
}

package com.group5.attendance.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
            throws Exception {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            String accept = req.getHeader("Accept");
            if ("GET".equals(req.getMethod()) && accept != null && accept.contains("text/html")) {
                String path = req.getRequestURI().substring(req.getContextPath().length());
                String query = req.getQueryString();
                req.getSession(true).setAttribute("returnTo", query == null ? path : path + "?" + query);
            }
            res.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        return true;
    }
}
package com.vifinancenews.controllers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import com.vifinancenews.services.UserService;
import jakarta.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Map;

public class AuthController {
    private static final UserService userService = new UserService();

    /** ✅ Register a new user */
    public static Handler register = ctx -> {
        try {
            // ✅ Debug log
            System.out.println("Incoming request: " + ctx.body());
    
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
    
            String email = requestBody.get("email");
            String password = requestBody.get("password");
            String userName = requestBody.get("userName");
            String avatarLink = requestBody.get("avatarLink");
            String bio = requestBody.get("bio");
    
            System.out.println("Received: " + email + ", " + password + ", " + userName);
    
            if (email == null || password == null || userName == null) {
                ctx.status(400).json(Map.of("error", "Missing required fields"));
                return;
            }
    
            boolean success = userService.registerUser(email, password, userName, avatarLink, bio);
    
            if (success) {
                ctx.status(201).json(Map.of("message", "Registration successful"));
            } else {
                ctx.status(400).json(Map.of("error", "Registration failed"));
            }
        } catch (Exception e) {
            e.printStackTrace();  // ✅ Print error in logs
            ctx.status(500).json(Map.of("error", "Internal server error", "details", e.getMessage()));
        }
    };
    

    /** ✅ Verify user credentials before OTP */
    public static Handler verifyCredentials = ctx -> {
        try {
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
            String email = requestBody.get("email");
            String password = requestBody.get("password");

            boolean isVerified = userService.verifyPassword(email, password);
            if (isVerified) {
                ctx.status(200).json(Map.of("message", "OTP sent"));
            } else {
                ctx.status(401).json(Map.of("error", "Invalid email or password"));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Server error", "details", e.getMessage()));
        }
    };

    /** ✅ Handle user login with OTP */
    public static Handler login = ctx -> {
        try {
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
            String email = requestBody.get("email");
            String otp = requestBody.get("otp");

            String userId = userService.login(email, otp);
            if (userId != null) {
                ctx.sessionAttribute("userId", userId); // ✅ Store userId in session
                ctx.status(200).json(Map.of("message", "Login successful", "userId", userId));
            } else {
                ctx.status(401).json(Map.of("error", "Invalid OTP"));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Server error", "details", e.getMessage()));
        }
    };

    /** ✅ Handle user logout */
    public static Handler logout = ctx -> {
        HttpSession session = ctx.req().getSession(false);
        if (session != null) {
            session.invalidate(); // ✅ Invalidate session
        }
        ctx.status(200).json(Map.of("message", "Logout successful"));
    };

    /** ✅ Check if user is logged in */
    public static Handler checkAuth = ctx -> {
        String userId = ctx.sessionAttribute("userId");
        if (userId != null) {
            ctx.json(Map.of("loggedIn", true, "userId", userId));
        } else {
            ctx.json(Map.of("loggedIn", false));
        }
    };
}

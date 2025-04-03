package com.vifinancenews.controllers;

import io.javalin.http.Handler;
import com.vifinancenews.services.UserService;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;

public class AuthController {
    private static final UserService userService = new UserService();

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
            e.printStackTrace(); 
            ctx.status(500).json(Map.of("error", "Internal server error", "details", e.getMessage()));
        }
    };
    

  
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


    public static Handler login = ctx -> {
        try {
            // Step 1: Get email and OTP from the request body
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
            String email = requestBody.get("email");
            String otp = requestBody.get("otp");
    
            // Step 2: Attempt login
            String userId = userService.login(email, otp);
    
            if (userId != null) {
                UUID uuid = UUID.fromString(userId);
                
                // Step 3: Handle soft-deleted account and reactivation logic
                boolean isSoftDeleted = userService.isAccountSoftDeleted(uuid);
                if (isSoftDeleted) {
                    boolean withinReactivationPeriod = userService.isWithinReactivationPeriod(uuid);
    
                    if (withinReactivationPeriod) {
                        // Notify frontend to show reactivation prompt
                        ctx.sessionAttribute("userId", userId);
                        ctx.status(200).json(Map.of(
                            "message", "Account is in reactivation period",
                            "actionRequired", "reactivate",
                            "userId", userId
                        ));
                    } else {
                        // Account is permanently deleted, inform the user
                        ctx.status(200).json(Map.of(
                            "message", "Account is permanently deleted",
                            "actionRequired", "none",
                            "userId", userId
                        ));
                    }
                } else {
                    // Normal login successful, set userId in session
                    ctx.sessionAttribute("userId", userId);
                    ctx.status(200).json(Map.of("message", "Login successful", "userId", userId));
                }
            } else {
                ctx.status(401).json(Map.of("message", "Invalid OTP"));
            }
    
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Server error", "details", e.getMessage()));
        }
    };
    


    // Reactivate account handler
    public static Handler reactivateAccount = ctx -> {
        System.out.println("Session data: " + ctx.sessionAttribute("userId"));
        String userId = ctx.sessionAttribute("userId");
        System.out.println("User ID from session: " + userId); // Debug log
        if (userId == null) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
    
        UUID uuid = UUID.fromString(userId);
        boolean reactivated = userService.restoreUser(uuid);
    
        if (reactivated) {
            ctx.status(200).json(Map.of("message", "Account reactivated successfully"));
        } else {
            ctx.status(400).json(Map.of("error", "Failed to reactivate account"));
        }
    };
    


    public static Handler logout = ctx -> {
        HttpSession session = ctx.req().getSession(false);
        if (session != null) {
            session.invalidate(); // ✅ Invalidate session
        }
        ctx.status(200).json(Map.of("message", "Logout successful"));
    };


    public static Handler checkAuth = ctx -> {
        String userId = ctx.sessionAttribute("userId");
        if (userId != null) {
            ctx.json(Map.of("loggedIn", true, "userId", userId));
        } else {
            ctx.json(Map.of("loggedIn", false));
        }
    };
}

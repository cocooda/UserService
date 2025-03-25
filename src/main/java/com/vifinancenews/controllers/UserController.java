package com.vifinancenews.controllers;

import com.vifinancenews.services.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.SQLException;

public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void registerRoutes(Javalin app) {
        System.out.println("Registering user routes...");
        app.post("/api/user/register", this::register);
        app.post("/api/user/login", this::login);
        app.get("/api/user/profile", this::getProfile);
        app.post("/api/user/logout", this::logout);
        app.delete("/api/user/delete", this::deleteUser);
    }

    private void register(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        String userName = ctx.formParam("userName");
        String avatarLink = ctx.formParam("avatarLink");
        String bio = ctx.formParam("bio");

        try {
            if (userService.registerUser(email, password, userName, avatarLink, bio)) {
                ctx.result("Registration successful.").status(200);
            } else {
                ctx.result("Registration failed.").status(400);
            }
        } catch (SQLException e) {
            ctx.result("Database error: " + e.getMessage()).status(500);
        }
    }

    private void login(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        String otp = ctx.formParam("otp");
    
        // Print received values for debugging
        System.out.println("Received login data - email: " + email + ", password: " + password + ", otp: " + otp);
    
        try {
            String userId = userService.login(email, password, otp); // Get the user ID from login method
    
            if (userId != null) {
                // Store the userId in session
                ctx.sessionAttribute("userId", userId);
                String sessionId = ctx.req().getSession().getId(); // ✅ Log session ID

                System.out.println(" Login successful! Stored userId: " + userId);
                System.out.println(" Session ID at login: " + sessionId);
                ctx.result("Login successful. Session started.").status(200);
            } else {
                ctx.result("Invalid credentials or OTP required.").status(400);
            }
        } catch (SQLException e) {
            ctx.result("Database error: " + e.getMessage()).status(500);
        }
    }
    

    private void getProfile(Context ctx) {
        String userId = ctx.sessionAttribute("userId");
        System.out.println("Stored userId: " + userId);
        if (userService.isAccountLoggedIn(userId)) {
            System.out.println("User is logged in, fetching profile...");
            ctx.result("User Profile - ID: " + userId).status(200);
        } else {
            System.out.println("User not logged in, access denied.");
            ctx.result("Access denied. Please log in to view your profile.").status(401);
        }
    }

    private void logout(Context ctx) {
        ctx.req().getSession().invalidate();
        System.out.println("User logged out.");
        ctx.result("You have been logged out successfully.").status(200);
    }

    private void deleteUser(Context ctx) {
        String email = ctx.formParam("email");
        try {
            if (userService.deleteUserByEmail(email)) {
                ctx.result("User deleted successfully.").status(200);
            } else {
                ctx.result("User not found.").status(400);
            }
        } catch (SQLException e) {
            ctx.result("Database error: " + e.getMessage()).status(500);
        }
    }
}

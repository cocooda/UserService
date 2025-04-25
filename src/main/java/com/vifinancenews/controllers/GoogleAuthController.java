package com.vifinancenews.controllers;

import com.vifinancenews.services.GoogleAuthService;
import com.vifinancenews.services.UserService;
import com.vifinancenews.models.Identifier;
import com.vifinancenews.daos.IdentifierDAO;
import io.javalin.http.Handler;
import java.util.Map;
import java.util.UUID;

public class GoogleAuthController {

    private static final UserService userService = new UserService();
    
    // Handler for Google Login
    public static Handler handleGoogleLogin = ctx -> {
        try {
            String idToken = ctx.body(); // Get the ID token from the request body (POST request)
            
            // Step 1: Validate the token and fetch the user's email
            String email = GoogleAuthService.getGoogleUserEmail(idToken);
            if (email == null) {
                ctx.status(400).json(Map.of("error", "Invalid Google token"));
                return;
            }
            
            // Step 2: Check if the user already exists (in your database) with that email
            Identifier existingUser = IdentifierDAO.getIdentifierByEmail(email);
            
            // Step 3: If the user does not exist, create a new account (you can skip password and OTP logic here)
            if (existingUser == null) {
                boolean success = userService.registerUser(email, null, email, null, null, "google");
                if (!success) {
                    ctx.status(400).json(Map.of("error", "Google login failed"));
                    return;
                }
                existingUser = IdentifierDAO.getIdentifierByEmail(email); // Retrieve the newly created user
            }
            
            // Step 4: Create a session
            UUID userId = existingUser.getId();
            ctx.sessionAttribute("userId", userId.toString());
            
            // Step 5: Respond with a success message
            ctx.status(200).json(Map.of("message", "Google login successful", "userId", userId.toString()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Google login error", "details", e.getMessage()));
        }
    };
}

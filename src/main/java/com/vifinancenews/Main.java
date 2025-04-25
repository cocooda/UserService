package com.vifinancenews;

import com.vifinancenews.controllers.AuthController;
import com.vifinancenews.controllers.AvatarController;
import com.vifinancenews.controllers.GoogleAuthController;
import com.vifinancenews.controllers.GuestController;
import com.vifinancenews.controllers.UserController;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class Main {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            // CORS and Routing Optimizations
            config.router.contextPath = "/";
            config.router.treatMultipleSlashesAsSingleSlash = true;

            // Enable Brotli & Gzip Compression
            config.http.brotliAndGzipCompression();

            // Static Files (relative to project root)
            config.staticFiles.add(staticFileConfig -> {
                staticFileConfig.hostedPath = "/"; // URL path for static files
                staticFileConfig.directory = "static"; // Ensure `/static` is accessible
                staticFileConfig.location = Location.CLASSPATH;
            });
        
            // Request Settings
            config.http.asyncTimeout = 30000; // 30 sec timeout
            config.http.maxRequestSize = 10_000_000L; // 10MB max request size
        }).start(7000);

        app.before(ctx -> {
            // Set Content Security Policy (CSP) header
            ctx.header("Content-Security-Policy", String.join("; ",
    "default-src 'self'",
    "script-src 'self' https://accounts.google.com https://apis.google.com https://www.gstatic.com 'unsafe-inline'",
    "style-src 'self' 'unsafe-inline'",
    "frame-src https://accounts.google.com https://apis.google.com",
    "connect-src 'self' https://www.googleapis.com https://accounts.google.com",
    "img-src 'self' https://lh3.googleusercontent.com data:"
));



            // CORS Settings
            ctx.header("Access-Control-Allow-Origin", "*"); // Allow all origins
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");

            // Session settings: Add cookie for session expiration
            ctx.cookie("session_timeout", String.valueOf(System.currentTimeMillis()), 600); // Session timeout in seconds
        });

        // **Auth Routes**
        app.post("/api/register", AuthController.register);
        app.post("/api/verify", AuthController.verifyCredentials); // Step 1: Verify email/password
        app.post("/api/login", AuthController.login); // Step 2: Login with OTP
        app.post("/api/logout", AuthController.logout);
        app.get("/api/auth-status", AuthController.checkAuth);
        app.post("/api/reactivate-account", AuthController.reactivateAccount);

        // **User Routes**
        app.get("/api/user/profile", UserController.getUserProfile);
        app.put("/api/user/update", UserController.updateUserProfile);
        app.delete("/api/user/delete", UserController.deleteUser);

        // **Guest Routes**
        GuestController.registerRoutes(app);

        app.post("/api/avatar/upload", AvatarController.uploadAvatar);
        app.post("/api/auth/google-login", GoogleAuthController.handleGoogleLogin);

        System.out.println("Server running on http://localhost:7000");
    }
}

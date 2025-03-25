package com.vifinancenews;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.http.Header;

import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.FileSessionDataStore;
import org.eclipse.jetty.server.session.NullSessionDataStore;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionHandler;

import com.vifinancenews.controllers.UserController;
import com.vifinancenews.services.UserService;

public class Main {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.jetty.sessionHandler(() -> new SessionHandler()); 
            config.staticFiles.add("/public", Location.CLASSPATH); // Serve frontend files if needed
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.allowHost("http://localhost:5500"); // Allow frontend domain
                    it.allowHost("http://127.0.0.1:5500"); // Allow alternative frontend URL
                    it.allowCredentials = true;
                    
                });
            });
        
            
        }).start(8080);


        app.before(ctx -> {
            System.out.println("➡️ Before request: " + ctx.method() + " " + ctx.path());
            System.out.println("➡️ Session ID (before): " + ctx.req().getSession().getId());
            System.out.println("➡️ User ID (before): " + ctx.sessionAttribute("userId"));
        });
        
        app.after(ctx -> {
            System.out.println("✅ After request: " + ctx.method() + " " + ctx.path());
            System.out.println("✅ Session ID (after): " + ctx.req().getSession().getId());
            System.out.println("✅ User ID (after): " + ctx.sessionAttribute("userId"));
        });
        
        UserService userService = new UserService();
        UserController userController = new UserController(userService);
        userController.registerRoutes(app);

        app.get("/", ctx -> ctx.result("Server is running!"));
    }
}

package com.vifinancenews.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class GuestController {
    
    public static void registerRoutes(Javalin app) {
        app.get("/", GuestController::homePage);
    }

    private static void homePage(Context ctx) {
        ctx.redirect("/login"); // Redirect guests to login page
    }
}

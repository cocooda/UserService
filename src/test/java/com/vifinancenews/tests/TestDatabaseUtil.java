package com.vifinancenews.tests;

import com.vifinancenews.services.UserService;
import com.vifinancenews.utilities.JwtUtil;
import com.vifinancenews.utilities.PasswordHash;
import com.vifinancenews.utilities.RedisOTPService;
import com.vifinancenews.models.Identifier;
import com.vifinancenews.daos.AccountDAO;
import com.vifinancenews.daos.IdentifierDAO;
import com.vifinancenews.models.Account;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;

class TestUserService {
    private final UserService userService = new UserService();

    @Test
    void testRetrieveIdentifiers() {
        try {
            List<Identifier> identifiers = IdentifierDAO.getAllIdentifiers();
            if (identifiers == null || identifiers.isEmpty()) {
                System.out.println("\n=== No Identifiers Found in Database ===");
            } else {
                System.out.println("\n=== Existing Identifiers in Database ===");
                identifiers.forEach(id ->
                        System.out.println("ID: " + id.getId() + ", Email: " + id.getEmail())
                );
            }
        } catch (SQLException e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testRetrieveAccounts() {
        try {
            List<Account> accounts = AccountDAO.getAllAccounts();
            if (accounts == null || accounts.isEmpty()) {
                System.out.println("\n=== No Accounts Found in Database ===");
            } else {
                System.out.println("\n=== Existing Accounts in Database ===");
                accounts.forEach(acc ->
                        System.out.println("Account ID: " + acc.getId() + ", Username: " + acc.getUserName())
                );
            }
        } catch (SQLException e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testRegisterUser() {
        String email = "bachduc.june@gmail.com";
        String rawPassword = "123456";
        String hashedPassword = PasswordHash.hashPassword(rawPassword);
        String userName = "TestUser";
        String avatarLink = null;
        String bio = "This is a test user.";

        try {
            System.out.println("\n=== Checking if test user already exists ===");
            userService.deleteUserByEmail(email);
            System.out.println("Existing user deleted if found.");

            System.out.println("\n=== Attempting to register user ===");
            boolean registerSuccess = userService.registerUser(email, hashedPassword, userName, avatarLink, bio);
            assertTrue(registerSuccess, "User registration should be successful!");
            System.out.println("User registered successfully!");
        } catch (SQLException e) {
            fail("Test failed due to SQLException: " + e.getMessage());
        }
    }

    @Test
    void testLoginFlowWithLogout() {
        String email = "bachduc.june@gmail.com";
        String rawPassword = "123456";

        try {
            System.out.println("\n=== Step 1: Initiating Login with Password ===");
            boolean loginInitiated = userService.initiateLogin(email, rawPassword);
            assertTrue(loginInitiated, "Login initiation should succeed if credentials are correct!");
            System.out.println("Password verified, OTP sent.");

            // Get OTP from Redis
            String storedOTP = RedisOTPService.getOTP(email);
            assertNotNull(storedOTP, "OTP should be stored in Redis!");
            System.out.println("Retrieved OTP from Redis: " + storedOTP);

            System.out.println("\n=== Step 2: Verifying OTP ===");
            String token = userService.verifyOTP(email, storedOTP);
            assertNotNull(token, "Valid OTP should return a JWT token!");
            System.out.println("Login successful! JWT Token: " + token);

            System.out.println("\n=== Step 4: Logging out ===");
            boolean logoutSuccess = userService.logout(token);
            assertTrue(logoutSuccess, "Logout should succeed!");
            System.out.println("User logged out successfully.");

            System.out.println("\n=== Step 5: Validating token after logout ===");
            boolean isTokenValid = JwtUtil.validateToken(token);
            assertFalse(isTokenValid, "Token should be invalid after logout!");
            System.out.println("Token invalidated successfully after logout.");

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Test failed due to SQLException: " + e.getMessage());
        }
    }
}

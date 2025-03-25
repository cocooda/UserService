/*package com.vifinancenews.tests;

import com.vifinancenews.services.UserService;
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
            boolean deleteSuccess = userService.deleteUserByEmail(email);
            System.out.println(deleteSuccess ? "Existing test user deleted." : "No existing test user found.");

            System.out.println("\n=== Attempting to register user ===");
            boolean registerSuccess = userService.registerUser(email, hashedPassword, userName, avatarLink, bio);
            assertTrue(registerSuccess, "User registration should be successful!");
            System.out.println("User registered successfully!");
        } catch (SQLException e) {
            fail("Test failed due to SQLException: " + e.getMessage());
        }
    }

    @Test
    void testLoginWithOTPFlow() {
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
            boolean otpVerified = userService.verifyOTP(email, storedOTP);
            assertTrue(otpVerified, "Correct OTP should allow login!");
            System.out.println("Login successful with correct OTP!");

            System.out.println("\n=== Step 3: Testing incorrect OTP ===");
            boolean wrongOtp = userService.verifyOTP(email, "123456");
            assertFalse(wrongOtp, "Login should fail with incorrect OTP!");
            System.out.println("Login failed as expected with incorrect OTP.");

        } catch (SQLException e) {
            e.printStackTrace();
            fail("Test failed due to SQLException: " + e.getMessage());
        }
    }

    @Test
    void testDeleteUser() {
        String email = "bachduc.june@gmail.com";
        try {
            boolean deleteSuccess = userService.deleteUserByEmail(email);
            assertTrue(deleteSuccess, "User deletion should be successful!");
            Identifier deletedUser = IdentifierDAO.getIdentifierByEmail(email);
            assertNull(deletedUser, "User should no longer exist in the database!");
        } catch (SQLException e) {
            fail("Test failed due to SQLException: " + e.getMessage());
        }
    }
}
*/
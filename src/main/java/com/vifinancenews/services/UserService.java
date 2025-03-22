package com.vifinancenews.services;

import com.vifinancenews.models.Identifier;
import com.vifinancenews.daos.AccountDAO;
import com.vifinancenews.daos.IdentifierDAO;
import com.vifinancenews.models.Account;
import com.vifinancenews.utilities.*;   

import java.sql.SQLException;
import java.time.LocalDateTime;

public class UserService {

    public boolean registerUser(String email, String passwordHash, String userName, String avatarLink, String bio) throws SQLException {
        // Create Identifier entry
        Identifier newIdentifier = IdentifierDAO.insertIdentifier(email, passwordHash);
        if (newIdentifier == null) return false;

        // Create Account entry with hashed ID
        Account newAccount = AccountDAO.insertAccount(newIdentifier.getId(), userName, avatarLink, bio);
        return newAccount != null;
    }

    public boolean deleteUserByEmail(String email) throws SQLException {
        Identifier user = IdentifierDAO.getIdentifierByEmail(email);
        if (user == null) return false;

        boolean accountDeleted = AccountDAO.deleteAccountByUserId(user.getId());
        boolean identifierDeleted = IdentifierDAO.deleteIdentifierByEmail(email);
        return accountDeleted && identifierDeleted;
    }

    public boolean initiateLogin(String email, String rawPassword) throws SQLException {
        Identifier user = IdentifierDAO.getIdentifierByEmail(email);
        if (user == null) {
            return false;
        }

        // Check if account is locked
        if (user.isLocked()) {
            System.out.println("Account is locked until: " + user.getLockoutUntil());
            return false;
        }

        // Verify password
        if (PasswordHash.verifyPassword(rawPassword, user.getPasswordHash())) {
            // Generate OTP
            String otp = OTPGenerator.generateOTP();

            // Store OTP in Redis
            RedisOTPService.storeOTP(email, otp);

            // Send OTP via email
            try {
                EmailUtility.sendOTP(email, otp);
            } catch (Exception e) {
                System.out.println("Error sending OTP: " + e.getMessage());
                return false;
            }

            System.out.println("OTP has been sent to " + email);

            // Reset failed attempts (only after OTP validation)
            IdentifierDAO.resetFailedAttempts(email);

            return true; // Password correct, OTP sent
        } else {
            // Increment failed attempts and lock if needed
            int newFailedAttempts = user.getFailedAttempts() + 1;
            LocalDateTime lockoutUntil = newFailedAttempts >= 5 ? LocalDateTime.now().plusMinutes(15) : null;
            IdentifierDAO.updateFailedAttempts(email, newFailedAttempts, lockoutUntil);
            return false;
        }
    }

    public String verifyOTP(String email, String enteredOTP) throws SQLException {
        boolean isValid = RedisOTPService.verifyOTP(email, enteredOTP);

        if (isValid) {
            IdentifierDAO.updateLastLogin(email);
            System.out.println("Login successful!");

            // Generate JWT Token after successful OTP verification
            return JwtUtil.generateToken(email);
        }

        System.out.println("Invalid or expired OTP.");
        return null;
    }

    public boolean logout(String token) {
        // Invalidate session by blacklisting the token in Redis (if needed)
        String email = JwtUtil.getEmailFromToken(token);
        if (email != null) {
            RedisOTPService.clearOTP(email);
            System.out.println("User logged out.");
            return true;
        }
        return false;
    }

    public boolean isAuthenticated(String token) {
        return JwtUtil.validateToken(token);
    }
}

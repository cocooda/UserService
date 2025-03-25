package com.vifinancenews.services;

import com.vifinancenews.models.Identifier;
import com.vifinancenews.daos.AccountDAO;
import com.vifinancenews.daos.IdentifierDAO;
import com.vifinancenews.models.Account;
import com.vifinancenews.utilities.EmailUtility;
import com.vifinancenews.utilities.PasswordHash;
import com.vifinancenews.utilities.OTPGenerator;
import com.vifinancenews.utilities.RedisOTPService;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class UserService {

    public boolean registerUser(String email, String password, String userName, String avatarLink, String bio) throws SQLException {
        String passwordHash = PasswordHash.hashPassword(password);
    
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

    public String login(String email, String password, String enteredOTP) throws SQLException {
        Identifier user = fetchUser(email);
        if (user == null) {
            System.out.println("User not found: " + email); // Debugging line
            return null; // Return null if user is not found
        }
    
        if (isAccountLocked(user)) {
            return null; // Return null if account is locked
        }
    
        boolean passwordMatches = verifyPassword(user, password);
        System.out.println("Password matches: " + passwordMatches);
        if (!passwordMatches) {
            return null; // Return null if password doesn't match
        }
    
        // OTP Handling
        if (enteredOTP == null || enteredOTP.isEmpty()) {
            sendOTP(email); // Send OTP if not entered
            return null; // Return null, as OTP is required
        }
    
        if (!verifyOTP(email, enteredOTP)) {
            return null; // Return null if OTP is incorrect
        }
    
        return user.getId().toString(); // Return the user ID if login is successful
    }
    

    private Identifier fetchUser(String email) throws SQLException {
        return IdentifierDAO.getIdentifierByEmail(email);
    }

    private boolean isAccountLocked(Identifier user) {
        if (user.isLocked()) {
            System.out.println("Account locked until: " + user.getLockoutUntil());
            return true;
        }
        return false;
    }

    private boolean verifyPassword(Identifier user, String password) throws SQLException {
        if (!PasswordHash.verifyPassword(password, user.getPasswordHash())) {
            handleFailedLoginAttempt(user);
            return false;
        }
        return true;
    }

    private void handleFailedLoginAttempt(Identifier user) throws SQLException {
        int newFailedAttempts = user.getFailedAttempts() + 1;
        LocalDateTime lockoutUntil = newFailedAttempts >= 5 ? LocalDateTime.now().plusMinutes(15) : null;
        IdentifierDAO.updateFailedAttempts(user.getEmail(), newFailedAttempts, lockoutUntil);
    }

    private void sendOTP(String email) {
        String otp = OTPGenerator.generateOTP();
        RedisOTPService.storeOTP(email, otp);
        System.out.println("Generated OTP: " + otp); // Log OTP for debugging
        try {
            EmailUtility.sendOTP(email, otp);
            System.out.println("OTP sent to " + email);
        } catch (Exception e) {
            System.out.println("Error sending OTP: " + e.getMessage());
        }
    }

    private boolean verifyOTP(String email, String enteredOTP) throws SQLException {
        if (!RedisOTPService.verifyOTP(email, enteredOTP)) {
            System.out.println("Invalid or expired OTP.");
            return false;
        }

        IdentifierDAO.resetFailedAttempts(email);
        return true;
    }

    public boolean isAccountLoggedIn(String userId) {
        // Dummy implementation of account status check
        // Ideally, this could involve a check to see if the user exists in the database
        return userId != null && !userId.isEmpty();
    }
}

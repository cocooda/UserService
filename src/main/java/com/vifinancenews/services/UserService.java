package com.vifinancenews.services;

import com.vifinancenews.models.Identifier;
import com.vifinancenews.config.DatabaseConfig;
import com.vifinancenews.daos.AccountDAO;
import com.vifinancenews.daos.IdentifierDAO;
import com.vifinancenews.models.Account;
import com.vifinancenews.utilities.EmailUtility;
import com.vifinancenews.utilities.PasswordHash;
import com.vifinancenews.utilities.OTPGenerator;
import com.vifinancenews.utilities.RedisOTPService;

import java.sql.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class UserService {

    // ========== Registration ==========
    public boolean registerUser(String email, String password, String userName, String avatarLink, String bio, String loginMethod) throws SQLException {
        String passwordHash = loginMethod.equals("local") ? PasswordHash.hashPassword(password) : null;
        Identifier newIdentifier = IdentifierDAO.insertIdentifier(email, passwordHash, loginMethod);
        if (newIdentifier == null) return false;
        Account newAccount = AccountDAO.insertAccount(newIdentifier.getId(), userName, avatarLink, bio);
        return newAccount != null;
    }

    public boolean createUserFromGoogle(String email, String userName, String avatarLink, String bio) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            // Begin transaction explicitly
            conn.setAutoCommit(false);
            
            // Create identifier (no password needed for Google login)
            Identifier newIdentifier = IdentifierDAO.insertIdentifier(email, null, "google");
            if (newIdentifier == null) {
                conn.rollback();
                return false; // Rollback on failure
            }
        
            // Create account with the random username
            Account newAccount = AccountDAO.insertAccount(newIdentifier.getId(), userName, avatarLink, bio);
            if (newAccount == null) {
                conn.rollback();
                return false; // Rollback on failure
            }
        
            // Commit the transaction if everything succeeds
            conn.commit();
            return true;
        } catch (SQLException e) {
            // In case of exception, rollback
            if (conn != null) {
                conn.rollback();
            }
            throw e;  // Re-throw exception after rollback
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    
    

    // ========== Local Login ==========
    public boolean verifyPassword(String email, String password) throws SQLException {
        Identifier user = fetchUser(email);
        if (user == null || !user.getLoginMethod().equalsIgnoreCase("local")) return false;

        if (isAccountLocked(user)) return false;

        boolean passwordMatches = PasswordHash.verifyPassword(password, user.getPasswordHash());
        if (!passwordMatches) {
            handleFailedLoginAttempt(user);
            return false;
        }

        sendOTP(email);
        return true;
    }

    public String login(String email, String enteredOTP) throws SQLException {
        Identifier user = fetchUser(email);
        if (user == null || !user.getLoginMethod().equalsIgnoreCase("local")) return null;

        if (!verifyOTP(email, enteredOTP)) return null;

        IdentifierDAO.resetFailedAttempts(email);
        return user.getId().toString();
    }

    // ========== Google Login ==========
    public String loginWithGoogle(String email) throws SQLException {
        Identifier user = fetchUser(email);
        if (user == null || !user.getLoginMethod().equalsIgnoreCase("google")) return null;

        // No OTP, no password check
        IdentifierDAO.resetFailedAttempts(email);
        return user.getId().toString();
    }

    // ========== Deletion & Restoration ==========
    public boolean deleteUserByEmail(String email) throws SQLException {
        Identifier user = IdentifierDAO.getIdentifierByEmail(email);
        if (user == null) return false;
        boolean accountDeleted = AccountDAO.deleteAccountByUserId(user.getId());
        boolean identifierDeleted = IdentifierDAO.deleteIdentifierByEmail(email);
        return accountDeleted && identifierDeleted;
    }

    public boolean deleteUserById(UUID userId) throws SQLException {
        boolean accountDeleted = AccountDAO.deleteAccountByUserId(userId);
        if (!accountDeleted) accountDeleted = AccountDAO.deleteFromDeletedAccounts(userId);
        boolean identifierDeleted = IdentifierDAO.deleteIdentifierByUserId(userId);
        return accountDeleted && identifierDeleted;
    }

    public boolean softDeleteUserById(UUID userId) throws SQLException {
        return AccountDAO.moveAccountToDeleted(userId);
    }

    public boolean isAccountSoftDeleted(UUID userId) throws SQLException {
        return AccountDAO.isAccountInDeleted(userId);
    }

    public boolean restoreUser(UUID userId) throws SQLException {
        return isWithinReactivationPeriod(userId) && AccountDAO.restoreUserById(userId);
    }

    // Check if the account is within the 30-day reactivation period
    public boolean isWithinReactivationPeriod(UUID userId) throws SQLException {
        Optional<LocalDateTime> deletedAt = AccountDAO.getDeletedAccountDeletedAt(userId);
        if (deletedAt.isPresent()) {
            LocalDateTime reactivationDeadline = deletedAt.get().plusDays(30);
            return LocalDateTime.now().isBefore(reactivationDeadline);
        }
        return false;
    }

    public static boolean permanentlyDeleteExpiredAccounts(int days) throws SQLException {
        boolean identifiersDeleted = IdentifierDAO.deleteExpiredIdentifiers(days);
        boolean accountsDeleted = AccountDAO.deleteExpiredDeletedAccounts(days);
        return identifiersDeleted || accountsDeleted;
    }

    // ========== Profile ==========
    public Account getUserProfile(UUID userId) throws SQLException {
        return AccountDAO.getAccountByUserId(userId);
    }

    public boolean updateUserProfile(UUID userId, String userName, String avatarLink, String bio) throws SQLException {
        return AccountDAO.updateAccount(userId, userName, avatarLink, bio);
    }

    // ========== Internal Utilities ==========
    public boolean emailExists(String email) throws SQLException {
        return IdentifierDAO.getIdentifierByEmail(email) != null;
    }

    public Identifier findByEmail(String email) throws SQLException {
        return IdentifierDAO.getIdentifierByEmail(email);
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

    private void handleFailedLoginAttempt(Identifier user) throws SQLException {
        int newFailedAttempts = user.getFailedAttempts() + 1;
        LocalDateTime lockoutUntil = newFailedAttempts >= 5 ? LocalDateTime.now().plusMinutes(15) : null;
        IdentifierDAO.updateFailedAttempts(user.getEmail(), newFailedAttempts, lockoutUntil);
    }

    private void sendOTP(String email) {
        String otp = OTPGenerator.generateOTP();
        RedisOTPService.storeOTP(email, otp);
        System.out.println("Generated OTP: " + otp);
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
}

package com.vifinancenews.user.services;

import com.vifinancenews.common.daos.AccountDAO;
import com.vifinancenews.common.daos.IdentifierDAO;
import com.vifinancenews.common.models.Account;
import com.vifinancenews.common.models.Identifier;
import com.vifinancenews.common.utilities.IDHash;
import com.vifinancenews.common.utilities.PasswordHash;
import com.vifinancenews.common.utilities.RedisCacheService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AccountService {

    //  ========== User Profile ==========

    public Account getUserProfile(UUID userId) throws SQLException {
        // Hash the userId to match the cache key
        String hashedUserId = IDHash.hashUUID(userId);
        
        // Check if user data is cached
        Map<String, String> cachedData = RedisCacheService.getCachedUserData(hashedUserId);
        if (cachedData != null) {
            // If data is cached, create and return Account object from cache
            return mapToAccount(cachedData, hashedUserId);  // Pass the hashed userId for creating Account
        }
    
        // If data is not cached, fetch from DB
        Account account = AccountDAO.getAccountByUserId(userId);
        if (account != null) {
            // Cache the user data after retrieving from DB
            RedisCacheService.cacheUserData(hashedUserId, mapAccountToCacheData(account));
        }
    
        return account;
    }
    
    
    /*//Old method (not used anymore)
    public boolean updateUserProfile(UUID userId, String userName, String avatarLink, String bio) throws SQLException {
        boolean updated = AccountDAO.updateAccount(userId, userName, avatarLink, bio);
        if (updated) {
            String hashedUserId = IDHash.hashUUID(userId);
        
            // Clear old cache and store updated data
            RedisCacheService.clearUserData(hashedUserId);
        
            Account updatedAccount = AccountDAO.getAccountByUserId(userId);
            RedisCacheService.cacheUserData(hashedUserId, mapAccountToCacheData(updatedAccount));
        }
        return updated;
    }*/

    // Update username and bio together
    public boolean updateUserNameAndBio(UUID userId, String userName, String bio) throws SQLException {
        boolean updated = AccountDAO.updateUsernameAndBio(userId, userName, bio);

        if (updated) {
            String hashedUserId = IDHash.hashUUID(userId);
            RedisCacheService.clearUserData(hashedUserId);

            Account updatedAccount = AccountDAO.getAccountByUserId(userId);
            RedisCacheService.cacheUserData(hashedUserId, mapAccountToCacheData(updatedAccount));
        }

        return updated;
    }

    // Update avatar link only
    public boolean updateAvatar(UUID userId, String avatarLink) throws SQLException {
        boolean updated = AccountDAO.updateAvatar(userId, avatarLink);

        if (updated) {
            String hashedUserId = IDHash.hashUUID(userId);
            RedisCacheService.clearUserData(hashedUserId);

            Account updatedAccount = AccountDAO.getAccountByUserId(userId);
            RedisCacheService.cacheUserData(hashedUserId, mapAccountToCacheData(updatedAccount));
        }

        return updated;
    }

    //  ========== Password Management ==========
    public boolean changePassword(UUID userId, String currentPassword, String newPassword) throws SQLException {
        Identifier user = IdentifierDAO.getIdentifierById(userId);
        if (user == null || !user.getLoginMethod().equalsIgnoreCase("local")) {
            return false;
        }

        boolean currentPasswordMatches = PasswordHash.verifyPassword(currentPassword, user.getPasswordHash());
        if (!currentPasswordMatches) {
            return false;
        }

        String newHashedPassword = PasswordHash.hashPassword(newPassword);
        return IdentifierDAO.changePassword(userId, newHashedPassword);
    }


    //  ========== Account Deletion ==========

    public boolean softDeleteUserById(UUID userId) throws SQLException {
        boolean deleted = AccountDAO.moveAccountToDeleted(userId);
        if (deleted) {
            // Clear cache after soft deletion
            RedisCacheService.clearUserData(userId.toString());
        }
        return deleted;
    }

    public boolean deleteUserByEmail(String email) throws SQLException {
        Identifier user = IdentifierDAO.getIdentifierByEmail(email);
        if (user == null) return false;
        boolean accountDeleted = AccountDAO.deleteAccountByUserId(user.getId());
        boolean identifierDeleted = IdentifierDAO.deleteIdentifierByEmail(email);
        if (accountDeleted && identifierDeleted) {
            // Clear cache after deletion
            RedisCacheService.clearUserData(user.getId().toString());
        }
        return accountDeleted && identifierDeleted;
    }

    public boolean deleteUserById(UUID userId) throws SQLException {
        boolean accountDeleted = AccountDAO.deleteAccountByUserId(userId);
        if (!accountDeleted) accountDeleted = AccountDAO.deleteFromDeletedAccounts(userId);
        boolean identifierDeleted = IdentifierDAO.deleteIdentifierByUserId(userId);
        if (accountDeleted && identifierDeleted) {
            // Clear cache after deletion
            RedisCacheService.clearUserData(userId.toString());
        }
        return accountDeleted && identifierDeleted;
    }

    public static boolean permanentlyDeleteExpiredAccounts(int days) throws SQLException {
        boolean identifiersDeleted = IdentifierDAO.deleteExpiredIdentifiers(days);
        boolean accountsDeleted = AccountDAO.deleteExpiredDeletedAccounts(days);
        return identifiersDeleted || accountsDeleted;
    }

    // Helper to map Account to Cache Data
    private Map<String, String> mapAccountToCacheData(Account account) {
        return Map.of(
            "userName", account.getUserName(),
            "avatarLink", account.getAvatarLink(),
            "bio", account.getBio()
        );
    }
    

    // Helper to map Cache Data to Account
    private Account mapToAccount(Map<String, String> data, String userId) {
        // Convert empty strings back to null for avatarLink and bio
        String avatarLink = data.get("avatarLink");
        String bio = data.get("bio");
    
        // If avatarLink or bio is an empty string, treat it as null
        if (avatarLink != null && avatarLink.isEmpty()) {
            avatarLink = null;
        }
        if (bio != null && bio.isEmpty()) {
            bio = null;
        }
    
        return new Account(
            userId,  // Since userId is passed separately, use it here
            data.get("userName"),
            avatarLink,
            bio
        );
    }
    
    
    
    
}

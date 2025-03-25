package com.vifinancenews.daos;

import com.vifinancenews.config.DatabaseConfig;
import com.vifinancenews.models.Account;
import com.vifinancenews.utilities.IDHash;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountDAO {

    public static List<Account> getAllAccounts() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String query = "SELECT id, username, avatar_link, bio FROM account";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Account account = new Account(
                    rs.getString("id"), 
                    rs.getString("username"), 
                    rs.getString("avatar_link"), 
                    rs.getString("bio")
                );
                accounts.add(account);
            }
        }
        return accounts.isEmpty() ? null : accounts;
    }

    public static Account insertAccount(UUID identifierId, String userName, String avatarLink, String bio) throws SQLException {
        String hashedId = IDHash.hashUUID(identifierId); // Hash UUID before storing

        String query = "INSERT INTO account (id, username, avatar_link, bio) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, hashedId);
            pstmt.setString(2, userName);
            pstmt.setString(3, avatarLink);
            pstmt.setString(4, bio);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                return new Account(hashedId, userName, avatarLink, bio);
            }
        }
        return null;
    }

    public static boolean deleteAccountByUserId(UUID identifierId) throws SQLException {
        String hashedId = IDHash.hashUUID(identifierId); // Hash before deletion
        String query = "DELETE FROM account WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, hashedId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}

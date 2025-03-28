package com.vifinancenews.utilities;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

public class RedisOTPService {
    private static final int OTP_EXPIRY_SECONDS = 300; // 5 minutes

    public static void storeOTP(String email, String otp) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String key = formatKey(email);
            jedis.setex(key, OTP_EXPIRY_SECONDS, otp);
            System.out.println("OTP stored in Redis for: " + email);
        } catch (JedisException e) {
            System.err.println("Redis error while storing OTP: " + e.getMessage());
        }
    }

    public static boolean verifyOTP(String email, String inputOTP) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            String key = formatKey(email);
            String storedOTP = jedis.get(key);

            if (storedOTP == null) {
                System.out.println("OTP expired or not found for: " + email);
                return false;
            }

            if (storedOTP.equals(inputOTP)) {
                jedis.del(key); // Delete OTP after successful verification
                System.out.println("OTP verified and deleted for: " + email);
                return true;
            }

            System.out.println("Invalid OTP for: " + email);
            return false;
        } catch (JedisException e) {
            System.err.println("Redis error while verifying OTP: " + e.getMessage());
            return false;
        }
    }

    public static void clearOTP(String email) {
        try (Jedis jedis = RedisConnection.getConnection()) {
            jedis.del(formatKey(email)); // Remove OTP from Redis
        }
    }

    private static String formatKey(String email) {
        return "otp:" + email.replace("@", "_").replace(".", "_");
    }
}

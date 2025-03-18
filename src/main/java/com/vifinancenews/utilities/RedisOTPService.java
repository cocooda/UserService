package com.vifinancenews.utilities;

import redis.clients.jedis.Jedis;

public class RedisOTPService {
    private static final int OTP_EXPIRY_SECONDS = 300; // 5 minutes

    public static void storeOTP(String email, String otp) {
        try (Jedis jedis = RedisConnection.connect()) {
            jedis.setex("otp:" + email, OTP_EXPIRY_SECONDS, otp);
        }
    }

    public static boolean verifyOTP(String email, String inputOTP) {
        try (Jedis jedis = RedisConnection.connect()) {
            String storedOTP = jedis.get("otp:" + email);
            if (storedOTP != null && storedOTP.equals(inputOTP)) {
                jedis.del("otp:" + email); // Delete OTP after successful login
                return true;
            }
            return false;
        }
    }

    // Add getOTP for TESTING ONLY
    public static String getOTP(String email) {
        try (Jedis jedis = RedisConnection.connect()) {
            return jedis.get("otp:" + email);
        }
    }
}

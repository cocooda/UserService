package com.vifinancenews.utilities;
import redis.clients.jedis.Jedis;

public class RedisConnection {
    private static final String REDIS_HOST = "127.0.0.1";
    private static final int REDIS_PORT = 6379;

    public static Jedis connect() {
        return new Jedis(REDIS_HOST, REDIS_PORT);
    }

}

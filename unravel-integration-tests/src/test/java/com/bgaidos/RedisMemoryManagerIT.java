package com.bgaidos;

import com.bgaidos.exceptions.SessionNotFoundException;
import com.bgaidos.service.sessions.api.MemoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = UnravelChallengeApp.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class RedisMemoryManagerIT {

    @Autowired
    @Qualifier("byteArrayRedis")
    private RedisTemplate<String, byte[]> redisTemplate;

    @Autowired
    private MemoryManager memoryManager;

    @BeforeEach
    void setUp() {
        assertNotNull(redisTemplate.getConnectionFactory());
        redisTemplate.getConnectionFactory().getConnection().flushAll(RedisServerCommands.FlushOption.SYNC);
    }

    @Test
    @DisplayName("1.0 - Test add session data success")
    void testAddSessionDataSuccess() {
        var sessionId = "SESSION_" + UUID.randomUUID();
        byte[] testData = "Test data content".getBytes();

        memoryManager.addSessionData(sessionId, testData);

        byte[] storedData = redisTemplate.opsForValue().get(sessionId);
        assertNotNull(storedData);
		assertArrayEquals(testData, storedData);
    }

    @Test
    @DisplayName("1.1 - Test session expiration after adding data")
    void testSessionExpiration() {
        var sessionId = "SESSION_" + UUID.randomUUID();
        byte[] testData = "Test data content".getBytes();

        memoryManager.addSessionData(sessionId, testData);

        var ttl = redisTemplate.getExpire(sessionId);
        assertNotNull(ttl);
        assertTrue(ttl > 0 && ttl <= Duration.ofMinutes(30).toSeconds(),
                "TTL should be positive and not exceed 30 minutes");
    }

    @Test
    @DisplayName("2.0 - Test remove session data success")
    void testRemoveSessionDataSuccess() {
        var sessionId = "SESSION_" + UUID.randomUUID();
        byte[] testData = "Test data content".getBytes();

        memoryManager.addSessionData(sessionId, testData);

        assertNotNull(redisTemplate.opsForValue().get(sessionId));

        memoryManager.removeSessionData(sessionId);

        assertNull(redisTemplate.opsForValue().get(sessionId));
    }

    @Test
    @DisplayName("3.0 - Test get session data success")
    void testGetSessionDataSuccess() {
        var sessionId = "SESSION_" + UUID.randomUUID();
        byte[] testData = "Test data content".getBytes();

        // Add data directly using Redis template
        redisTemplate.opsForValue().set(sessionId, testData, Duration.ofMinutes(30));

        // Retrieve using memory manager
        byte[] retrievedData = memoryManager.getSessionData(sessionId);
        
        assertNotNull(retrievedData);
        assertTrue(Arrays.equals(testData, retrievedData));
    }

    @Test
    @DisplayName("3.1 - Test expiration refresh on data retrieval")
    void testExpirationRefreshOnRetrieval() {
        var sessionId = "SESSION_" + UUID.randomUUID();
        byte[] testData = "Test data content".getBytes();

        redisTemplate.opsForValue().set(sessionId, testData, Duration.ofSeconds(5));

        var initialTtl = redisTemplate.getExpire(sessionId);

        memoryManager.getSessionData(sessionId);

        var newTtl = redisTemplate.getExpire(sessionId);
        
        assertNotNull(initialTtl);
        assertNotNull(newTtl);
        assertTrue(newTtl > initialTtl, "TTL should be refreshed and longer after retrieval");
        assertTrue(newTtl <= Duration.ofMinutes(30).toSeconds(), "New TTL should not exceed 30 minutes");
    }

    @Test
    @DisplayName("3.2 - Test get session data not found")
    void testGetSessionDataNotFound() {
        var nonExistentSessionId = "NON_EXISTENT_SESSION";

        var exception = assertThrows(SessionNotFoundException.class,
                () -> memoryManager.getSessionData(nonExistentSessionId));

        assertEquals("No session data found for session ID: %s.".formatted(nonExistentSessionId), 
                exception.getMessage());
    }

    @Test
    @DisplayName("4.1 - Stress test LRU behavior under high load")
    void testLRUBehaviorUnderHighLoad() {
        var sessionIdFormat = "SESSION_%s" + UUID.randomUUID();
        byte[] sessionData = new byte[1024 * 1024 * 10]; // 10 MB data;

        for (int i = 0; i < 10; i++) {
            var sessionId = sessionIdFormat.formatted(i);
            memoryManager.addSessionData(sessionId, sessionData);
        }

        var exception = assertThrows(SessionNotFoundException.class,
            () -> memoryManager.getSessionData(sessionIdFormat.formatted(1)));
        assertEquals("No session data found for session ID: %s.".formatted(sessionIdFormat.formatted(1)),
            exception.getMessage());
    }
}
package com.bgaidos.service.sessions.redis;

import com.bgaidos.exceptions.SessionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class RedisMemoryManagerTest {

    @Mock
    private RedisTemplate<String, byte[]> byteArrayRedisTemplate;

    @Mock
    private ValueOperations<String, byte[]> valueOps;

    private RedisMemoryManager redisMemoryManager;

    @BeforeEach
    void setUp() {
        redisMemoryManager = new RedisMemoryManager(byteArrayRedisTemplate);
    }

    @Test
    @DisplayName("1.0 - Test addSessionData success")
    void testAddSessionDataSuccess() {
        var sessionId = "SESSION_123";
        byte[] data = "test data".getBytes();

        when(byteArrayRedisTemplate.opsForValue()).thenReturn(valueOps);

        redisMemoryManager.addSessionData(sessionId, data);

        verify(valueOps).set(eq(sessionId), eq(data), eq(Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("1.1 - Test addSessionData with null sessionId")
    void testAddSessionDataNullSessionId() {
        byte[] data = "test data".getBytes();

        assertThrows(NullPointerException.class, () -> {
            redisMemoryManager.addSessionData(null, data);
        });
    }

    @Test
    @DisplayName("1.2 - Test addSessionData with null data")
    void testAddSessionDataNullData() {
        var sessionId = "SESSION_123";

        when(byteArrayRedisTemplate.opsForValue()).thenReturn(valueOps);

        redisMemoryManager.addSessionData(sessionId, new byte[]{0});

        verify(valueOps).set(eq(sessionId), eq(new byte[]{0}), eq(Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("2.0 - Test removeSessionData success")
    void testRemoveSessionDataSuccess() {
        var sessionId = "SESSION_123";

        redisMemoryManager.removeSessionData(sessionId);

        verify(byteArrayRedisTemplate).delete(eq(sessionId));
    }

    @Test
    @DisplayName("2.1 - Test removeSessionData with null sessionId")
    void testRemoveSessionDataNullSessionId() {
        assertThrows(NullPointerException.class, () -> {
            redisMemoryManager.removeSessionData(null);
        });
    }

    @Test
    @DisplayName("3.0 - Test getSessionData success")
    void testGetSessionDataSuccess() {
        var sessionId = "SESSION_123";
        byte[] expectedData = "test data".getBytes();

        when(byteArrayRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(eq(sessionId))).thenReturn(expectedData);

        byte[] actualData = redisMemoryManager.getSessionData(sessionId);

        assertNotNull(actualData);
        assertArrayEquals(expectedData, actualData);
        verify(valueOps).get(eq(sessionId));
        verify(byteArrayRedisTemplate).expire(eq(sessionId), eq(Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("3.1 - Test getSessionData with null sessionId")
    void testGetSessionDataNullSessionId() {
        assertThrows(NullPointerException.class, () -> {
            redisMemoryManager.getSessionData(null);
        });
    }

    @Test
    @DisplayName("3.2 - Test getSessionData when session not found")
    void testGetSessionDataSessionNotFound() {
        var sessionId = "SESSION_123";

        when(byteArrayRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(eq(sessionId))).thenReturn(null);

        SessionNotFoundException exception = assertThrows(SessionNotFoundException.class, () -> {
            redisMemoryManager.getSessionData(sessionId);
        });

        assertEquals("No session data found for session ID: %s.".formatted(sessionId), exception.getMessage());
        verify(valueOps).get(eq(sessionId));
    }
}

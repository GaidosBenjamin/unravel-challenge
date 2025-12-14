package com.bgaidos;

import com.bgaidos.exceptions.SessionConflictException;
import com.bgaidos.exceptions.SessionNotFoundException;
import com.bgaidos.service.sessions.api.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = UnravelChallengeApp.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class RedisSessionManagerIT {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private SessionManager sessionManager;

	@BeforeEach
	void setUp() {
		assertNotNull(redisTemplate.getConnectionFactory());
		redisTemplate.getConnectionFactory().getConnection().flushAll();
	}

	@Test
	@DisplayName("1.0 - Test login success")
	void testSaveSessionSuccess() {
		var userId = "testUser";

		var sessionDetails = sessionManager.saveSession(userId);

		assertNotNull(sessionDetails);
		assertEquals(userId, sessionDetails.userId());
		assertTrue(sessionDetails.sessionId().startsWith("SESSION_"));

		var key = String.format("user:session:%s", userId);
		var sessionId = redisTemplate.opsForValue().get(key);
		assertEquals(sessionDetails.sessionId(), sessionId);
	}

	@Test
	@DisplayName("1.1 - Test login conflict")
	void testSaveSessionConflict() {
		var userId = "testUser";

		sessionManager.saveSession(userId);

		var exception = assertThrows(SessionConflictException.class,
			() -> sessionManager.saveSession(userId));

		assertEquals(String.format("User %s is already logged in.", userId), exception.getMessage());
	}

	@Test
	@DisplayName("1.2 - Test session expiration after login")
	void testSessionExpiration() {
		var userId = "testUser";
		sessionManager.saveSession(userId);

		var key = String.format("user:session:%s", userId);
		var ttl = redisTemplate.getExpire(key);

		assertNotNull(ttl);
		assertTrue(ttl > 0 && ttl <= Duration.ofHours(1).toSeconds(),
			"TTL should be positive and not exceed 1 hour");
	}

	@Test
	@DisplayName("2.0 - Test logout success")
	void testRemoveSessionSuccess() {
		var userId = "testUser";
		sessionManager.saveSession(userId);

		var logoutResult = sessionManager.removeSession(userId);

		assertTrue(logoutResult.isRemoved());

		var key = String.format("user:session:%s", userId);
		var sessionId = redisTemplate.opsForValue().get(key);
		assertNull(sessionId);
	}

	@Test
	@DisplayName("3.0 - Get session details ")
	void testGetSessionDetailsSuccess() {
		var userId = "testUser";
		var loginDetails = sessionManager.saveSession(userId);

		var sessionDetails = sessionManager.getSessionDetails(userId);

		assertNotNull(sessionDetails);
		assertEquals(userId, sessionDetails.userId());
		assertEquals(loginDetails.sessionId(), sessionDetails.sessionId());
	}

	@Test
	@DisplayName("3.1 - Get session details not found")
	void testGetSessionDetailsNotFound() {
		var userId = "nonExistentUser";

		SessionNotFoundException exception = assertThrows(SessionNotFoundException.class,
			() -> sessionManager.getSessionDetails(userId));

		assertEquals(String.format("No session found for user: %s.", userId), exception.getMessage());
	}
}


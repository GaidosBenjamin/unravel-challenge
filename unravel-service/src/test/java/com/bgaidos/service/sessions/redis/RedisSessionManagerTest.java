package com.bgaidos.service.sessions.redis;

import com.bgaidos.exceptions.SessionConflictException;
import com.bgaidos.exceptions.SessionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class RedisSessionManagerTest {

	@Mock
	private StringRedisTemplate redisSession;
	@Mock
	private ValueOperations<String, String> valueOps;

	private RedisSessionManager redisSessionManager;

	@BeforeEach
	void setUp() {
		redisSessionManager = new RedisSessionManager(redisSession);
	}

	@Test
	@DisplayName("1.0 - Test login success")
	void testSaveSessionSuccess() {
		var userId = "user123";

		when(redisSession.opsForValue())
			.thenReturn(valueOps);
		when(valueOps.setIfAbsent(anyString(), anyString(), any()))
			.thenReturn(true);

		var sessionDetails = redisSessionManager.saveSession(userId);

		assertNotNull(sessionDetails);
		assertEquals(userId, sessionDetails.userId());
		assertNotNull(sessionDetails.sessionId());
		verify(valueOps).setIfAbsent(
			eq("user:session:%s".formatted(userId)),
			startsWith("SESSION_"),
			eq(Duration.ofHours(1))
		);
	}

	@Test
	@DisplayName("1.1 - Test login failure due to null userId")
	void testSaveSessionNullUserId() {
		var exception = assertThrows(NullPointerException.class,
			() -> redisSessionManager.saveSession(null));

		assertEquals("userId is marked non-null but is null", exception.getMessage());
	}

	@Test
	@DisplayName("1.2 - Test login failure due user already logged in")
	void testSaveSessionConflictSession() {
		var userId = "user123";

		when(redisSession.opsForValue())
			.thenReturn(valueOps);
		when(valueOps.setIfAbsent(anyString(), anyString(), any()))
			.thenReturn(false);

		var exception = assertThrows(SessionConflictException.class,
			() -> redisSessionManager.saveSession(userId));

		assertEquals("User %s is already logged in.".formatted(userId), exception.getMessage());
		verify(valueOps).setIfAbsent(
			eq("user:session:%s".formatted(userId)),
			startsWith("SESSION_"),
			eq(Duration.ofHours(1))
		);
	}

	@Test
	@DisplayName("2.0 - Test logout success")
	void removeSessionSuccess() {
		var userId = "user123";

		var response = redisSessionManager.removeSession(userId);

		assertNotNull(response);
		assertEquals(true, response.isRemoved());
		verify(redisSession).delete("user:session:%s".formatted(userId));
	}

	@Test
	@DisplayName("2.1 - Test logout failure due to null userId")
	void testRemoveSessionNullUserId() {
		var exception = assertThrows(NullPointerException.class,
			() -> redisSessionManager.removeSession(null));

		assertEquals("userId is marked non-null but is null", exception.getMessage());
	}

	@Test
	@DisplayName("3.0 - Test getSessionDetails success")
	void testGetSessionDetailsSuccess() {
		var userId = "user123";
		var sessionId = "SESSION_ABC123";

		when(redisSession.opsForValue())
			.thenReturn(valueOps);
		when(valueOps.get(anyString()))
			.thenReturn(sessionId);

		var sessionDetails = redisSessionManager.getSessionDetails(userId);

		assertNotNull(sessionDetails);
		assertEquals(userId, sessionDetails.userId());
		assertEquals(sessionId, sessionDetails.sessionId());
		verify(valueOps).get("user:session:%s".formatted(userId));
		verify(redisSession).expire("user:session:%s".formatted(userId), Duration.ofHours(1));
	}

	@Test
	@DisplayName("3.1 - Test getSessionDetails failure due to null userId")
	void testGetSessionDetailsNullUserId() {
		var exception = assertThrows(NullPointerException.class,
			() -> redisSessionManager.getSessionDetails(null));

		assertEquals("userId is marked non-null but is null", exception.getMessage());
	}

	@Test
	@DisplayName("3.2 - Test getSessionDetails failure due session not found")
	void testGetSessionDetailsSessionNotFound() {
		var userId = "user123";

		when(redisSession.opsForValue())
			.thenReturn(valueOps);
		when(valueOps.get(anyString()))
			.thenReturn(null);

		var exception = assertThrows(SessionNotFoundException.class,
			() -> redisSessionManager.getSessionDetails(userId));

		assertEquals("No session found for user: %s.".formatted(userId), exception.getMessage());
		verify(valueOps).get("user:session:%s".formatted(userId));
	}
}
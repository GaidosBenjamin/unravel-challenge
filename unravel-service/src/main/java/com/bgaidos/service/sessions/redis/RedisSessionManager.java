package com.bgaidos.service.sessions.redis;

import com.bgaidos.api.RemoveDto;
import com.bgaidos.api.SessionDetailsDto;
import com.bgaidos.exceptions.SessionNotFoundException;
import com.bgaidos.exceptions.SessionConflictException;
import com.bgaidos.service.sessions.api.SessionManager;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class RedisSessionManager implements SessionManager {

	private final StringRedisTemplate redisSession;

	public static final String KEY_PREFIX = "user:session:%s";
	public static final String SESSION_PREFIX = "SESSION_%s";
	public static final Duration SESSION_DURATION = Duration.ofHours(1);

	public RedisSessionManager(@Qualifier("stringRedis") StringRedisTemplate redisSession) {
		this.redisSession = redisSession;
	}

	@Override
	public SessionDetailsDto saveSession(@NonNull String userId) {
		var userSession = KEY_PREFIX.formatted(userId);
		var sessionId = SESSION_PREFIX.formatted(UUID.randomUUID());

		var isSuccess = redisSession.opsForValue()
			.setIfAbsent(userSession, sessionId, SESSION_DURATION);

		if (!isSuccess) {
			throw new SessionConflictException("User %s is already logged in.".formatted(userId));
		}

		return new SessionDetailsDto(userId, sessionId);
	}

	@Override
	public RemoveDto removeSession(@NonNull String userId) {
		var userSession = KEY_PREFIX.formatted(userId);
		redisSession.delete(userSession);
		return new RemoveDto(true);
	}

	@Override
	public SessionDetailsDto getSessionDetails(@NonNull String userId) {
		var userSession = KEY_PREFIX.formatted(userId);
		var sessionId = redisSession.opsForValue().get(userSession);

		if (sessionId == null) {
			throw new SessionNotFoundException("No session found for user: %s.".formatted(userId));
		}

		//Refreshes the session expiration
		redisSession.expire(userSession, SESSION_DURATION);

		return new SessionDetailsDto(userId, sessionId);
	}
}

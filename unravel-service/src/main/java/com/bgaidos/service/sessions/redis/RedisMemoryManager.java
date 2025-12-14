package com.bgaidos.service.sessions.redis;

import com.bgaidos.exceptions.SessionNotFoundException;
import com.bgaidos.service.sessions.api.MemoryManager;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisMemoryManager implements MemoryManager {

	private final RedisTemplate<String, byte[]> byteArrayRedisTemplate;

	private static final Duration SESSION_DURATION = Duration.ofMinutes(30);

	public RedisMemoryManager(@Qualifier("byteArrayRedis") RedisTemplate<String, byte[]> byteArrayRedisTemplate) {
		this.byteArrayRedisTemplate = byteArrayRedisTemplate;
	}

	@Override
	public void addSessionData(@NonNull String sessionId, byte[] data) {
		byteArrayRedisTemplate.opsForValue()
			.set(sessionId, data, SESSION_DURATION);
	}

	@Override
	public void removeSessionData(@NonNull String sessionId) {
		byteArrayRedisTemplate.delete(sessionId);
	}

	@Override
	public byte[] getSessionData(@NonNull String sessionId) {
		byte[] data = byteArrayRedisTemplate.opsForValue().get(sessionId);

		if (data != null) {
			byteArrayRedisTemplate.expire(sessionId, SESSION_DURATION); //Refresh expiration
		} else {
			throw new SessionNotFoundException("No session data found for session ID: %s.".formatted(sessionId));
		}

		return data;
	}
}

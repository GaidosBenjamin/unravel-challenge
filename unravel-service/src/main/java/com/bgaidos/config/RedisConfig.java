package com.bgaidos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

	@Bean
	public RedisTemplate<String, byte[]> byteArrayRedis(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, byte[]> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(RedisSerializer.string());
		template.setHashKeySerializer(RedisSerializer.string());
		template.setValueSerializer(RedisSerializer.byteArray());
		template.setHashValueSerializer(RedisSerializer.byteArray());
		return template;
	}

	@Bean
	public StringRedisTemplate stringRedis(RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}

}

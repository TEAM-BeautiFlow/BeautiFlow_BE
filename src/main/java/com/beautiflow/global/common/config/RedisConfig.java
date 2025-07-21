package com.beautiflow.global.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import com.beautiflow.chat.service.RedisPubSubService;
import com.beautiflow.global.common.Alert.AlertEventListener;

@Configuration
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String host;

	@Value("${spring.data.redis.port}")
	private int port;

	//연결 기본 객체
	@Bean
	@Qualifier("chatPubSub")
	public RedisConnectionFactory chatPubSubFactory() {
		RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
		configuration.setHostName(host);
		configuration.setPort(port);
		//redis pub/sub 에서는 특정 데이터베이스에 의존적이지 않음.
		//configuration.setDatabase(0);
		return new LettuceConnectionFactory(configuration);
	}

	//publish 객체
	@Bean
	@Qualifier("chatPubSub")
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return new StringRedisTemplate(redisConnectionFactory);
	}

	//subscribe 객체
	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(
		@Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory,
		MessageListenerAdapter messageListenerAdapter
	) {
		RedisMessageListenerContainer container=new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		container.addMessageListener(messageListenerAdapter, new PatternTopic("chat"));
		return container;
	}


	//redis에서 수신된 메시지를 처리하는 객체 생성
	@Bean
	public MessageListenerAdapter messageListenerAdapter(RedisPubSubService redisPubSubService){
		//RedisPubSubService의 특정 메서드가 수신된 메시지를 처리할 수 있도록 지정
		return new MessageListenerAdapter(redisPubSubService,"onMessage");
	}

	// 알림용 리스너 등록
	@Bean(name = "alertListenerAdapter")
	public MessageListenerAdapter alertListenerAdapter(AlertEventListener listener) {
		return new MessageListenerAdapter(listener, "handle");
	}

	// 알림용 리스너 컨테이너 등록 (옵션)
	@Bean
	public RedisMessageListenerContainer alertListenerContainer(
		@Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory,
		@Qualifier("alertListenerAdapter") MessageListenerAdapter alertListenerAdapter
	) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		container.addMessageListener(alertListenerAdapter, new PatternTopic("alertQueue"));
		return container;
	}

}
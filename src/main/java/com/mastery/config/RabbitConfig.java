package com.mastery.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
public class RabbitConfig {
    
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_QUEUE = "order.queue";
    public static final String SECKILL_EXCHANGE = "seckill.exchange";
    public static final String SECKILL_QUEUE = "seckill.queue";
    
    @Bean public TopicExchange orderExchange() { return new TopicExchange(ORDER_EXCHANGE, true, false); }
    @Bean public TopicExchange seckillExchange() { return new TopicExchange(SECKILL_EXCHANGE, true, false); }
    
    @Bean public Queue orderQueue() { return QueueBuilder.durable(ORDER_QUEUE).deadLetterExchange("dlx.exchange").deadLetterRoutingKey("dlx.key").build(); }
    @Bean public Queue seckillQueue() { return QueueBuilder.durable(SECKILL_QUEUE).build(); }
    @Bean public Queue dlxQueue() { return QueueBuilder.durable("dlx.queue").build(); }
    
    @Bean public DirectExchange dlxExchange() { return new DirectExchange("dlx.exchange", true, false); }
    @Bean public Binding dlxBinding() { return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with("dlx.key"); }
    @Bean public Binding orderBinding() { return BindingBuilder.bind(orderQueue()).to(orderExchange()).with("order.*"); }
    @Bean public Binding seckillBinding() { return BindingBuilder.bind(seckillQueue()).to(seckillExchange()).with("seckill.*"); }
    @Bean public MessageConverter messageConverter() { return new Jackson2JsonMessageConverter(); }
}

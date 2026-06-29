package com.mastery.service;

import com.mastery.config.RabbitConfig;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnBean(name = "rabbitTemplate")
public class OrderMessageConsumer {
    
    @RabbitListener(queues = RabbitConfig.SECKILL_QUEUE)
    public void handleSeckill(Map<String, Object> msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Seckill order: {}", msg);
        channel.basicAck(tag, false);
    }
    
    @RabbitListener(queues = RabbitConfig.ORDER_QUEUE)
    public void handleOrder(Object order, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Order created: {}", order);
        channel.basicAck(tag, false);
    }
}

package com.mastery.service;

import com.mastery.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.features.seckill", name = "enabled", havingValue = "true")
public class SeckillService {
    
    private final StringRedisTemplate redisTemplate;
    
    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;
    
    public SeckillService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    private static final String STOCK_PREFIX = "seckill:stock:";
    private static final String ORDER_PREFIX = "seckill:order:";
    private static final String RESULT_PREFIX = "seckill:result:";
    
    private static final long ORDER_MARK_TTL_MINUTES = 30;

    private static final String LUA = """
        local stock = redis.call('get', KEYS[1])
        if not stock or tonumber(stock) <= 0 then return -1 end
        if redis.call('exists', KEYS[2]) == 1 then return -2 end
        redis.call('decr', KEYS[1])
        redis.call('set', KEYS[2], '1', 'EX', ARGV[1])
        return 1
        """;
    
    public Map<String, Object> seckill(Long productId, Long userId) {
        String stockKey = STOCK_PREFIX + productId;
        String orderKey = ORDER_PREFIX + productId + ":" + userId;

        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(LUA, Long.class),
                List.of(stockKey, orderKey),
                String.valueOf(TimeUnit.MINUTES.toSeconds(ORDER_MARK_TTL_MINUTES))
        );
        if (result == null) {
            return Map.of("status", "fail", "msg", "system busy");
        }
        if (result == -1L) {
            return Map.of("status", "fail", "msg", "sold out");
        }
        if (result == -2L) {
            return Map.of("status", "fail", "msg", "already participated");
        }

        if (rabbitTemplate != null) {
            rabbitTemplate.convertAndSend(RabbitConfig.SECKILL_EXCHANGE, "seckill.order", Map.of("productId", productId, "userId", userId));
        } else {
            redisTemplate.opsForValue().set(RESULT_PREFIX + userId, String.valueOf(System.currentTimeMillis()));
        }
        
        return Map.of("status", "pending", "msg", "in queue, please wait...");
    }
    
    public Map<String, Object> getResult(Long userId) {
        String key = RESULT_PREFIX + userId;
        String val = redisTemplate.opsForValue().get(key);
        if (val == null) return Map.of("status", "pending");
        redisTemplate.delete(key);
        return Map.of("status", "success", "orderId", val);
    }
}

package top.dzou.qrcode_scan_websocket.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author dingxiang
 * @date 19-9-2 下午6:55
 */

@Service
@Slf4j
public class RedisPoolFactory {

    @Autowired
    RedisConfig redisConfig;

    @Bean
    public JedisPool JedisPoolFactory() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        log.info("{} {} {} {} {} {}", redisConfig.getHost(), redisConfig.getPassword(), redisConfig.getPoolMaxActive(), redisConfig.getPort(), redisConfig.getPoolMaxIdle(), redisConfig.getTimeout());
        poolConfig.setMaxIdle(redisConfig.getPoolMaxIdle());
        poolConfig.setMaxTotal(redisConfig.getPoolMaxActive());
        poolConfig.setMaxWaitMillis(redisConfig.getPoolMaxWait() * 1000);
        log.info("config:{}", poolConfig);
        JedisPool jp = new JedisPool(poolConfig, redisConfig.getHost(), redisConfig.getPort(),
                redisConfig.getTimeout(), redisConfig.getPassword(), 0);
        return jp;
    }

}

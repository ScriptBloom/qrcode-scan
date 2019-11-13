package top.dzou.qrcode_scan_websocket.redis;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author dingxiang
 * @date 19-9-2 下午6:54
 */

@Data
@Component
//@ConfigurationProperties(prefix = "redis")
public class RedisConfig {
    @Value("${redis.host}")
    private String host;
    @Value("${redis.port}")
    private int port;
    @Value("${redis.timeout}")
    private int timeout;//秒
    @Value("${redis.password}")
    private String password;
    @Value("${redis.pool-max-active}")
    private int poolMaxActive;
    @Value("${redis.pool-max-idle}")
    private int poolMaxIdle;
    @Value("${redis.pool-max-wait}")
    private int poolMaxWait;//秒
}
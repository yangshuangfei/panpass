package com.panpass.gateway;

import com.panpass.commons.template.RedisRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@SpringBootTest
@ComponentScan({"com.panpass.commons.redis"})
class TdbGatewayApplicationTests {

    @Resource
    private RedisRepository redisRepository;
    @Test
    void contextLoads() {
        redisRepository.set("12344","121321");
    }

}

package com.panpass.gateway.cache;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.panpass.commons.template.RedisRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @Description: TODO
 * @ClassName RedisIgnoreUrl
 * @Author So_Ea
 * @Date 2021/2/2
 * @ModDate 2021/2/2
 * @ModUser So_Ea
 * @Version V1.0
 **/
@Component
public class RedisIgnoreUrl {
    @Resource
    private RedisRepository redisRepository;
    private static final String REDIS_IGNORE_API_URL_KEY="IGNORE:OAUTH_IGNORE_API_URL";
    private static final String REDIS_IGNORE_WEB_URL_KEY="IGNORE:OAUTH_IGNORE_WEB_URL";

    public String[] getIgnoreUrls(){
        List<Object> ignoreApiList = redisRepository.getList(REDIS_IGNORE_API_URL_KEY,0,-1);
        List<Object> ignoreWebList = redisRepository.getList(REDIS_IGNORE_WEB_URL_KEY,0,-1);
        ignoreWebList.stream().forEach(obj -> {
            ignoreApiList.add(obj);
        });
        String[] strings = ignoreApiList.toArray(new String[ignoreApiList.size()]);
        return strings;
    }
}

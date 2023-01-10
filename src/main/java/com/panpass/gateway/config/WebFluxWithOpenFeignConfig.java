package com.panpass.gateway.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.stream.Collectors;

/**
 * @Description: TODO
 * @ClassName WebFluxWithOpenFeignConfig
 * @Author So_Ea
 * @Date 2021/3/25
 * @ModDate 2021/3/25
 * @ModUser So_Ea
 * @Version V1.0
 **/
@Configuration
public class WebFluxWithOpenFeignConfig {
    @Bean
    @ConditionalOnMissingBean
    public HttpMessageConverters messageConverters(ObjectProvider<HttpMessageConverter<?>> converters) {
        return new HttpMessageConverters(converters.orderedStream().collect(Collectors.toList()));
    }
}

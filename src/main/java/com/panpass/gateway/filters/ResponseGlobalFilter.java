package com.panpass.gateway.filters;

import cn.hutool.core.util.ObjectUtil;
import com.panpass.commons.oauth.properties.SecurityProperties;
import com.panpass.gateway.handler.CommonResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * @Description: 统一返回值处理
 * @ClassName ResponseGlobalFilter
 * @Author So_Ea
 * @Date 2021/1/27
 * @ModDate 2021/1/27
 * @ModUser So_Ea
 * @Version V1.0
 **/
@Slf4j
@Component
public class ResponseGlobalFilter implements GlobalFilter, Ordered {

    @Resource
    private SecurityProperties securityProperties;
    private static final String SWAGGER_API_URL="API-DOCS";
    private AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        String requestUrl = exchange.getRequest().getPath().toString();
        if(requestUrl.toUpperCase().contains(SWAGGER_API_URL)){
            return chain.filter(exchange.mutate().response(originalResponse).build());
        }

        for (String ignoreUrl : securityProperties.getResponseIgnoreUrl().getHttpUrls()) {
            if(antPathMatcher.match(ignoreUrl,requestUrl)){
                return chain.filter(exchange.mutate().response(originalResponse).build());
            }
        }

        ServerHttpResponseDecorator decoratedResponse = new CommonResponseHandler(originalResponse,exchange.getRequest());
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }

}

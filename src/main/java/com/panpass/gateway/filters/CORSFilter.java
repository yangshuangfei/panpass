package com.panpass.gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

/**
 * @Description: TODO
 * @ClassName CorsResponseHeaderFilter
 * @Author So_Ea
 * @Date 2021/6/24
 * @ModDate 2021/6/24
 * @ModUser So_Ea
 * @Version V1.0
 **/
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CORSFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange swe, WebFilterChain wfc) {
        ServerHttpRequest request = swe.getRequest();
        if (CorsUtils.isCorsRequest(request)) {
            ServerHttpResponse response = swe.getResponse();
            HttpHeaders headers = response.getHeaders();
            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "*");
            headers.add("Access-Control-Max-Age", "3600");
            headers.add("Access-Control-Allow-Headers", "*");
           /* headers.add("Access-Control-Allow-Headers", "Content-Type");*/
            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }
        }
        return wfc.filter(swe);
    }
}

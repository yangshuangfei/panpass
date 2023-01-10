package com.panpass.gateway.component;

import cn.hutool.json.JSONUtil;
import com.panpass.gateway.enums.CommonResult;
import lombok.AllArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

/**
 * 
 * @ClassName: RestfulAccessDeniedHandler
 * @Description: 当访问接口没有权限时，自定义的返回结果
 * @author bin.ants
 * @date 2019年6月26日
 *
 */
@Component
@AllArgsConstructor
public class RestfulAccessDeniedHandler implements ServerAccessDeniedHandler {
	@Override
	public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.OK);
		response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		String body= JSONUtil.toJsonStr(new CommonResult().forbidden(denied.getMessage()));
		DataBuffer buffer =  response.bufferFactory().wrap(body.getBytes(Charset.forName("UTF-8")));
		return response.writeWith(Mono.just(buffer));
	}
}

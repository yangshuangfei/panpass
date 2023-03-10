package com.panpass.gateway.filters;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.panpass.commons.biz.CurrentUser;
import com.panpass.commons.contanst.AuthContanst;
import com.panpass.commons.util.CurrentUserUtil;
import com.panpass.gateway.biz.vo.ResponseVO;
import com.panpass.gateway.enums.ReturnCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.DefaultServerRequest;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @Description: ?????????????????????
 * @ClassName ResponseGlobalFilter
 * @Author So_Ea
 * @Date 2021/1/27
 * @ModDate 2021/1/27
 * @ModUser So_Ea
 * @Version V1.0
 **/
@Slf4j
@Component
public class RequestGlobalFilter implements GlobalFilter, Ordered {

    /*private final static String X_REAL_IP = "X-Real-IP";*/
    /*private static final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();*/

    /*@Resource
    private TokenStore tokenStore;*/

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try{
            // ?????????????????????????????????
            MediaType mediaType = exchange.getRequest().getHeaders().getContentType();
            ServerRequest serverRequest = new DefaultServerRequest(exchange);
            log.info("????????????URL:{}",exchange.getRequest().getURI());
            // ?????????json????????????body???????????????object or map ??????
            if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)){
                log.info("1");
                Mono<Object> modifiedBody = serverRequest.bodyToMono(Object.class)
                        .flatMap(body -> {
                            recordLog(exchange.getRequest(), body);
                            return Mono.just(body);
                        });

                return getVoidMonoBody(exchange, chain, Object.class, modifiedBody);
            }else if(MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)){
                log.info("2");
                Mono<String> modifiedBody = serverRequest.bodyToMono(String.class)
                        // .log("modify_request_mono", Level.INFO)
                        .flatMap(body -> {
                            recordLog(exchange.getRequest(), body);

                            return Mono.just(body);
                        });

                return getVoidMonoBody(exchange, chain, String.class, modifiedBody);
            }
            // ????????????????????????????????????body??????Get????????????
            recordLog(exchange.getRequest(), "");
            List<String> token = exchange.getRequest().getHeaders().get(AuthContanst.OAUTH_AUTHROIZATION);
            if(CollectionUtil.isNotEmpty(token)){
                CurrentUser currentUser = CurrentUserUtil.getCurrentUserInfo();
                ServerHttpRequest request = exchange.getRequest().mutate().headers(httpHeaders -> {
                    httpHeaders.add(AuthContanst.OAUTH_X_USER, currentUser.getId().toString());
                    httpHeaders.add(AuthContanst.OAUTH_X_USER_NAME, currentUser.getUsername());
                    if(StringUtils.isNotBlank(currentUser.getAgentId())){
                        httpHeaders.add(AuthContanst.OAUTH_X_AGENT_ID, currentUser.getAgentId());
                    }
                }).build();
                log.info("3");
                return chain.filter(exchange.mutate().request(request).build());
            }
            log.info("4");
            return chain.filter(exchange.mutate().request(exchange.getRequest()).build());
        }finally {
            CurrentUserUtil.clearUserContext();
            /*log.info("????????????????????????{}   ???????????????????????????{}",exchange.getRequest().getURI(),CurrentUserUtil.getCurrentUserInfo());*/
        }
    }

    @Override
    public int getOrder() {
        /*return NettyWriteResponseFilter.HIGHEST_PRECEDENCE ;*/
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 2;
    }

    private void recordLog(ServerHttpRequest request, Object body) {
        // ??????????????????url
        StringBuilder builder = new StringBuilder(" ??????Request Url: ");
        builder.append(request.getURI().getRawPath());
        // ?????????????????????
        HttpMethod method = request.getMethod();
        if (null != method){
            builder.append(", method: ").append(method.name());
        }


        // ??????????????????
        builder.append(", header { ");
        for (Map.Entry<String, List<String>> entry : request.getHeaders().entrySet()) {
            builder.append(entry.getKey()).append(":").append(StringUtils.join(entry.getValue(), ",")).append(",");
        }

        // ????????????
        builder.append("} param: ");
        // ??????get?????????
        if (null != method && HttpMethod.GET.matches(method.name())) {
            // ??????????????????????????? ??????GET ??????
            MultiValueMap<String, String> queryParams = request.getQueryParams();
            for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                builder.append(entry.getKey()).append("=").append(StringUtils.join(entry.getValue(), ",")).append(",");
            }
        }else {
            // ???body???????????????
            builder.append(body);
        }
        log.info(builder.toString());
    }



    private Mono<Void> getVoidMonoBody(ServerWebExchange exchange, GatewayFilterChain chain, Class outClass, Mono<?> modifiedBody) {
        BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, outClass);
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchange.getRequest().getHeaders());
        headers.remove(HttpHeaders.CONTENT_LENGTH);
        List<String> token = exchange.getRequest().getHeaders().get(AuthContanst.OAUTH_AUTHROIZATION);
        if(CollectionUtil.isNotEmpty(token)){
            CurrentUser currentUser = CurrentUserUtil.getCurrentUserInfo();
            headers.add(AuthContanst.OAUTH_X_USER, currentUser.getId().toString());
            headers.add(AuthContanst.OAUTH_X_USER_NAME, currentUser.getUsername());
            if(StringUtils.isNotBlank(currentUser.getAgentId())){
                headers.add(AuthContanst.OAUTH_X_AGENT_ID, currentUser.getAgentId());
            }
        }
        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
        return bodyInserter.insert(outputMessage,  new BodyInserterContext())
                .then(Mono.defer(() -> {
                    ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(
                            exchange.getRequest()) {
                        @Override
                        public HttpHeaders getHeaders() {
                            long contentLength = headers.getContentLength();
                            HttpHeaders httpHeaders = new HttpHeaders();
                            httpHeaders.putAll(headers);
                            if (contentLength > 0) {
                                httpHeaders.setContentLength(contentLength);
                            } else {
                                httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                            }
                            return httpHeaders;
                        }

                        @Override
                        public Flux<DataBuffer> getBody() {
                            return outputMessage.getBody();
                        }
                    };
                    return chain.filter(exchange.mutate().request(decorator).build());
                }));
    }

}

package com.panpass.gateway.handler;

import com.panpass.gateway.component.RestAuthenticationEntryPoint;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
/*import org.springframework.boot.web.error.ErrorAttributeOptions;*/
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
/*import org.springframework.http.HttpStatus;*/
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
/*import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;*/
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: TODO
 * @ClassName GlobalExceptionHandler
 * @Author So_Ea
 * @Date 2021/1/27
 * @ModDate 2021/1/27
 * @ModUser So_Ea
 * @Version V1.0
 **/
public class GlobalExceptionHandler extends DefaultErrorWebExceptionHandler {

    @Resource
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    public GlobalExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        if(throwable instanceof InvalidTokenException){
            return restAuthenticationEntryPoint.commence(exchange,new BadCredentialsException("当前登录信息失效,请退出后重新登录"));
        }
        return Mono.error(throwable);
    }

    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        int code = 500;
        Throwable error = super.getError(request);
        if (error instanceof org.springframework.cloud.gateway.support.NotFoundException) {
            code = 404;
        }
        return response(code,this.buildMessage(request, error));
    }
    /*protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        int code = 500;
        Throwable error = super.getError(request);
        if (error instanceof org.springframework.cloud.gateway.support.NotFoundException) {
            code = 404;
        }
        return response(code, this.buildMessage(request, error));
    }*/

    /**
     * 指定响应处理方法为JSON处理的方法
     * @param errorAttributes
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * 根据code获取对应的HttpStatus
     * @param errorAttributes
     */
    @Override
    protected HttpStatus getHttpStatus(Map<String, Object> errorAttributes) {
        /*int statusCode = (int) errorAttributes.get("code");*/
        return HttpStatus.OK;
    }

    /**
     * 构建返回的JSON数据格式
     * @param status		状态码
     * @param errorMessage  异常信息
     * @return
     */
    public static Map<String, Object> response(int status, String errorMessage) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", status);
        map.put("msg", errorMessage);
        map.put("data", null);
        return map;
    }

    /**
     * 构建异常信息
     * @param request
     * @param ex
     * @return
     */
    private String buildMessage(ServerRequest request, Throwable ex) {
        StringBuilder message = new StringBuilder("Failed to handle request [");
        message.append(request.methodName());
        message.append(" ");
        message.append(request.uri());
        message.append("]");
        if (ex != null) {
            message.append(": ");
            message.append(ex.getMessage());
        }
        return message.toString();
    }
}

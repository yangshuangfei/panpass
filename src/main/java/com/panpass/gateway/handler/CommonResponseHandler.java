package com.panpass.gateway.handler;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.panpass.gateway.biz.vo.ResponseVO;
import com.panpass.gateway.enums.ReturnCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @Description: TODO
 * @ClassName CommonResponseHandler
 * @Author So_Ea
 * @Date 2021/1/27
 * @ModDate 2021/1/27
 * @ModUser So_Ea
 * @Version V1.0
 **/
@Slf4j
public class CommonResponseHandler extends ServerHttpResponseDecorator {
    private DataBufferFactory bufferFactory ;
    private ServerHttpRequest serverHttpRequest ;
    public CommonResponseHandler(ServerHttpResponse delegate, ServerHttpRequest serverHttpRequest) {
        super(delegate);
        this.bufferFactory = delegate.bufferFactory();
        this.serverHttpRequest = serverHttpRequest;
    }
    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        if(body instanceof Flux){
            Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
            return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                DataBuffer join = dataBufferFactory.join(dataBuffers);
                byte[] content = new byte[join.readableByteCount()];
                join.read(content);;
                // 释放掉内存
                DataBufferUtils.release(join);
                String resultData = new String(content, Charset.forName("UTF-8"));
                ResponseVO response = ResponseVO.restResult(ReturnCodeEnum.SUCCESS.getCode(),ReturnCodeEnum.SUCCESS.getMsg(),
                        JSON.parse(resultData));
                byte[] newRs = JSON.toJSONString(response).getBytes(Charset.forName("UTF-8"));
                this.getDelegate().getHeaders().setContentLength(newRs.length);//如果不重新设置长度则收不到消息。
                log.info("响应请求：{}   时间：{}  ",serverHttpRequest.getPath(),System.currentTimeMillis());
                return bufferFactory.wrap(newRs);
            }));
        }
        return super.writeWith(body);
    }


    /**
     * 获取请求体中的字符串内容
     * @param serverHttpRequest
     * @return
     */
    /*private String resolveBodyFromRequest(ServerHttpRequest serverHttpRequest){
        //获取请求体
        Flux<DataBuffer> body = serverHttpRequest.getBody();
        StringBuilder sb = new StringBuilder();

        body.subscribe(buffer -> {
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            DataBufferUtils.release(buffer);
            String bodyString = new String(bytes, StandardCharsets.UTF_8);
            sb.append(bodyString);
        });
        return sb.toString();

    }*/
    public static void main(String[] args) {
        String sss="132121321122231287";
        ResponseVO response = ResponseVO.restResult(ReturnCodeEnum.SUCCESS.getCode(),ReturnCodeEnum.SUCCESS.getMsg(),
                JSON.parse(sss));
        System.out.println(JSON.toJSON(response));
    }
}

package com.panpass.gateway.biz.vo;

import com.panpass.gateway.enums.ReturnCodeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Description: TODO
 * @ClassName ResponseVO
 * @Author So_Ea
 * @Date 2021/1/27
 * @ModDate 2021/1/27
 * @ModUser So_Ea
 * @Version V1.0
 **/
@Data
@Accessors(chain = true)
public class ResponseVO<T> implements Serializable {
    private static final long serialVersionUID = 5005318709330004756L;
    /**
     * 状态码
     */
    private Integer code;
    /**
     * 描述
     */
    private String msg;
    /**
     * 结果集
     */
    private T data;

    public static <T> ResponseVO<T> ok() {
        return restResult(ReturnCodeEnum.SUCCESS, null);
    }

    public static <T> ResponseVO<T> ok(T data) {
        return restResult(ReturnCodeEnum.SUCCESS, data);
    }

    public static <T> ResponseVO<T> ok(ReturnCodeEnum errorCodeEnum, T data) {
        return restResult(errorCodeEnum, data);
    }

    public static <T> ResponseVO<T> failed(ReturnCodeEnum errorCodeEnum) {
        return restResult(errorCodeEnum, null);
    }

    public static <T> ResponseVO<T> failed(int code, String msg) {
        return restResult(code, msg, null);
    }

    private static <T> ResponseVO<T> restResult(ReturnCodeEnum returnCodeEnum, T data) {
        return restResult(returnCodeEnum.getCode(), returnCodeEnum.getMsg(), data);
    }

    public static <T> ResponseVO<T> restResult(Integer code, String msg, T data) {
        ResponseVO<T> apiResult = new ResponseVO<>();
        apiResult.setCode(code);
        apiResult.setMsg(msg);
        apiResult.setData(data);
        return apiResult;
    }

}

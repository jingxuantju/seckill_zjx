package com.jingxuan.seckill.exception;


import com.jingxuan.seckill.vo.RespBeanEnum;
import lombok.Data;

/**
 * 全局异常
 *
 * @ClassName: GlobalException
 */
@Data
public class GlobalException extends RuntimeException {

    private RespBeanEnum respBeanEnum;

    public RespBeanEnum getRespBeanEnum() {
        return respBeanEnum;
    }

    public void setRespBeanEnum(RespBeanEnum respBeanEnum) {
        this.respBeanEnum = respBeanEnum;
    }

    public GlobalException(RespBeanEnum respBeanEnum) {
        this.respBeanEnum = respBeanEnum;
    }
}

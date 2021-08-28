package com.yueyedexue.gulimall.product.exception;

import com.yueyedexue.common.exception.BizCodeEnum;
import com.yueyedexue.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

/**
 * @description: 统一异常处理
 * @author: MoonNightSnow
 * @createTime: 2021/7/30 8:33
 **/
@Slf4j
@RestControllerAdvice(basePackages = "com.yueyedexue.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.warn("数据校验失败{}, 异常类型{}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        HashMap<String, String> map = new HashMap<>();
        bindingResult.getFieldErrors().forEach((fieldError -> {
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
        }));
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data", map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleThrowable(Throwable throwable) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg", throwable.getMessage());
        map.put("throwType", throwable.getClass().getSimpleName());
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMsg())
                .put("data", map);
    }
}

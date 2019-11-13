package top.dzou.qrcodescan.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import top.dzou.qrcodescan.model.ErrorCodeEnum;
import top.dzou.qrcodescan.model.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author dingxiang
 * @date 19-9-3 下午1:38
 */

/**
 * 全局异常处理
 */
@ControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)//拦截所有异常
    public Result<String> exceptionHandler(HttpServletRequest request, Exception e) {
        e.printStackTrace();
        if (e instanceof BindException) {
            BindException ex = (BindException) e;
            List<ObjectError> errors = ex.getAllErrors();
            String error = errors.get(0).toString();
            String reg = "[^\u4e00-\u9fa5]";//使用正则提取错误中的中文信息
            String str = error.replaceAll(reg, "");
            return Result.fillArgs(ErrorCodeEnum.BIND_ERROR, str);//带参数异常
        } else if (e instanceof GlobalException) {
            GlobalException ex = (GlobalException) e;
            return Result.error(ex.getI());
        } else {
            log.error("其他异常");
            return Result.error(ErrorCodeEnum.NET_ERROR);
        }
    }
}

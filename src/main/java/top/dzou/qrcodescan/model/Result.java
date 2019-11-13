package top.dzou.qrcodescan.model;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.io.Serializable;

/**
 * @author dingxiang
 * @date 19-9-2 下午4:53
 */
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 3956528717501837568L;

    private boolean success;

    private int code;

    private String msg;

    private T data;

    public Result() {
    }

    public Result(boolean success, int code, String msg, T data) {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> error(IError error) {
        return error(error.getCode(), error.getMessage());
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<T>(false, code, message, null);
    }


    public static <T> Result<T> success(T data) {
        return new Result<T>(true, ErrorCodeEnum.SUCCESS.getCode(), ErrorCodeEnum.SUCCESS.getMessage(), data);
    }


    public static Result<String> ok() {
        return success("ok");
    }


    public String toJson() {
        return JSON.toJSONString(this);
    }

    public static <T> Result<T> fillArgs(IError iError, Object... args) {
        return error(iError.getCode(), String.format(iError.getMessage(), args));
    }

}

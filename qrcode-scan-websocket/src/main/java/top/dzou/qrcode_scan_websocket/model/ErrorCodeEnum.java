package top.dzou.qrcode_scan_websocket.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author dingxiang
 * @date 19-9-2 下午4:53
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum implements IError {

    /**
     * 100X  用户相关错误
     * //
     */
//    ILLEGAL_CODE(1000, "code不合法"),
//    TOKEN_NOT_EXIST(1001, "token不存在"),
//    ILLEGAL_TOKEN(1002, "token非法"),
//    TOKEN_EXPIRED(1003, "token已过期"),
//    USER_NOT_EXIST(1004, "用户不存在"),
//    USER_ID_EXPIRED(1005, "用户身份已过期"),
    PASSWORD_EMPTY(1001, "密码能不为空"),
    MOBILE_PATTERN_WRONG(1002, "手机号格式错误"),
    MOBILE_NOT_EXIST(1003, "手机号不存在"),
    LOGIN_FAIL(1004, "登陆失败,密码错误"),
    USER_NOT_EXSIT(1005, "用户不存在"),
    NOT_LOGIN(1006,"尚未登录"),


    /**
     * 000X 公共错误
     */
    SUCCESS(2000, "OK"),
    NET_ERROR(2001, "网络错误"),
    PARAM_ERROR(2002, "参数错误"),
    AUTHORITY_NOT_ENOUGH(2003, "权限不足"),
    SYSTEM_BUSY(2004, "系统繁忙"),
    BIND_ERROR(2005, "参数异常:%s"),
    REQUEST_ERROR(2006,"请求错误"),
    REQUEST_TOO_MUCH(2007,"操作太频繁"),
    SEVER_ERROR(2008,"服务器异常"),


    /**
     * 400X 业务错误
     */
    UUID_EXPIRED(4001, "uuid已经过期"),
    QRCODE_SCANNED(4002, "uuid已经被扫描，请重新获取"),
    ;


    private int code;

    private String message;
}

package top.dzou.qrcodescan.exception;


import top.dzou.qrcodescan.model.IError;

/**
 * @author dingxiang
 * @date 19-9-3 下午1:50
 */
public class GlobalException extends RuntimeException {
    private IError i;

    public GlobalException(IError i) {
        this.i = i;
    }

    public IError getI() {
        return i;
    }
}

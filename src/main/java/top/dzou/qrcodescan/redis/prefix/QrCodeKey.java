package top.dzou.qrcodescan.redis.prefix;

import lombok.NonNull;
import top.dzou.qrcodescan.redis.BasePrefix;

/**
 * @author dingxiang
 * @date 19-11-9 下午2:43
 */
public class QrCodeKey extends BasePrefix {
    public QrCodeKey(@NonNull String prefix, int expireSeconds) {
        super(prefix, expireSeconds);
    }
    public QrCodeKey(String prefix){super(prefix);}

    public static QrCodeKey UUID = new QrCodeKey("uuid",300);
}

package top.dzou.qrcode_scan_websocket.utils;

import java.util.UUID;

/**
 * @author dingxiang
 * @date 19-9-3 下午2:43
 */
public class UUIDUtil {
    public static String uuid() {
        return UUID.randomUUID().toString();
    }
}

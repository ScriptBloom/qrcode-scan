package top.dzou.qrcode_scan_websocket.redis.prefix;


import top.dzou.qrcode_scan_websocket.redis.BasePrefix;

/**
 * @author dingxiang
 * @date 19-9-2 下午11:36
 */
public class UserKey extends BasePrefix {

    public UserKey(String prefix, int expireSeconds) {
        super(prefix, expireSeconds);
    }

    public UserKey(String prefix) {
        super(prefix);
    }

    public static UserKey getById = new UserKey("id");

    public static UserKey getByName = new UserKey("name");
}

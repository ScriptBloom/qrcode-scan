package top.dzou.qrcodescan.redis.prefix;


import top.dzou.qrcodescan.redis.BasePrefix;

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

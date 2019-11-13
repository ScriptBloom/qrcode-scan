package top.dzou.qrcodescan.redis;

/**
 * @author dingxiang
 * @date 19-9-2 下午11:32
 */

/**
 * redis key的前缀 用于分辨不同类不同获取方法 比如用户(name、id)、订单
 */
public interface KeyPrefix {
    public int expireSeconds();

    public String getPrefix();

}

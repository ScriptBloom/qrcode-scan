package top.dzou.qrcodescan.service;

import java.io.IOException;

/**
 * @author dingxiang
 * @date 19-11-9 下午2:57
 */
public interface QrCodeService {

    String createQrCode(String content,int width,int height) throws IOException;
}

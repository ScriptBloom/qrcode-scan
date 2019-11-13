package top.dzou.qrcodescan.model;

/**
 * @author dingxiang
 * @date 19-11-9 下午2:37
 */
public enum QrCodeStatus {
    NOT_SCAN(1,"not_scan"),
    SCANNED(2,"scanned"),
    VERIFIED(3,"verified"),
    EXPIRED(4,"expired"),
    FINISH(5,"finish");

    private final String status;
    QrCodeStatus(int i, String finish) {
        this.status = finish;
    }

    public String getStatus() {
        return status;
    }
}

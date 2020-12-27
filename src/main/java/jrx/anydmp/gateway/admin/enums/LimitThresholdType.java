package jrx.anydmp.gateway.admin.enums;

/**
 * @author zhao tingting
 * @date 2018/10/24
 */
public enum LimitThresholdType {

    // 限流控制-阈值类型
    QPS(0,"QPS"),
    THREAD_CNT(1,"线程数")
    ;

    private int code;

    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    LimitThresholdType(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

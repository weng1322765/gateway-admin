package jrx.anydmp.gateway.admin.enums;

/**
 * @author zhao tingting
 * @date 2018/10/24
 */
public enum DegradeThresholdType {

    // 降级控制-阈值类型
    RT(0,"RT"),
    EXCEPTION_PERCENT(1,"异常比例")
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

    DegradeThresholdType(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

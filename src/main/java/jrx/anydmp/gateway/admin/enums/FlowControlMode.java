package jrx.anydmp.gateway.admin.enums;

/**
 * @author zhao tingting
 * @date 2018/10/24
 */
public enum FlowControlMode {

    // 流控模式
    FAIL(0,"失败"),
    WARM_UP(1,"WARM UP"),
    WAIT(2,"排队等待")
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

    FlowControlMode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

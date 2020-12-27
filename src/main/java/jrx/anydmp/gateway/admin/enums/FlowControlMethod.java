package jrx.anydmp.gateway.admin.enums;

/**
 * @author zhao tingting
 * @date 2018/10/24
 */
public enum FlowControlMethod {

    // 流控方式
    DIRECT(0,"直接"),
    RELATE(1,"关联"),
    LINK(2,"链路")
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

    FlowControlMethod(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

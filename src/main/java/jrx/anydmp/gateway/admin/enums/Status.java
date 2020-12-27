package jrx.anydmp.gateway.admin.enums;

/**
 * @author zhao tingting
 * @date 2018/10/24
 */
public enum Status {

    // 状态
    ENABLE(1,"启用"),
    DISABLE(0,"禁用"),
    DELETE(-1,"删除")

    ;

    private Integer code;

    private String message;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    Status(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}

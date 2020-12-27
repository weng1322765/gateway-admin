package jrx.anydmp.gateway.admin.enums;

/**
 * 应用实例状态
 * @author zhangyingxuan
 * @date 2018-09-20
 **/
public enum InstanceStatus {

    // 未知状态
    UNKNOWN(0, 1, "未知状态"),
    STARTING(1, 1, "启动中"),
    UP(2, 2, "已上线"),
    OUT_OF_SERVICE(3, 4, "暂停服务"),
    DOWN(4, 4, "已下线"),
    DELETE(5, 1, "删除")
    ;

    /**
     * 枚举值
     */
    private int value;
    /**
     * 枚举备注说明
     */
    private String memo;

    /**
     * 通知级别  {@link NoticeLevel}
     */
    private int noticeLevel;

    public int getValue() {
        return value;
    }

    public String getMemo() {
        return memo;
    }

    public int getNoticeLevel() {
        return noticeLevel;
    }

    InstanceStatus(int value, int noticeLevel, String memo) {
        this.value = value;
        this.noticeLevel = noticeLevel;
        this.memo = memo;
    }


}

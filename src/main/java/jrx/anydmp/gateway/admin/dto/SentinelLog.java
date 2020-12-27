package jrx.anydmp.gateway.admin.dto;

import java.io.Serializable;

public class SentinelLog implements Serializable {
    private static final long serialVersionUID = -8977555691071138892L;

    private String datetime;//时间
    private String resource;
    private String passQps;//每秒通过请求
    private String blockQps;//每秒被拦截请求
    private String finishQps;//每秒正常结束+异常结束总数
    private String exceptionQps;//每秒异常数
    private String avgRt;//平均响应时间

    public SentinelLog(){

    }

    public SentinelLog(String datetime, String resourceId, String passQps,
                       String blockQps, String finishQps, String exceptionQps, String avgRt) {
        this.datetime = datetime;
        this.resource = resourceId;
        this.passQps = passQps;
        this.finishQps = finishQps;
        this.blockQps = blockQps;
        this.exceptionQps = exceptionQps;
        this.avgRt = avgRt;
    }

    public String getDatetime() {
        return datetime;
    }

    public SentinelLog setDatetime(String datetime) {
        this.datetime = datetime;
        return this;
    }

    public String getResource() {
        return resource;
    }

    public SentinelLog setResource(String resource) {
        this.resource = resource;
        return this;
    }

    public String getPassQps() {
        return passQps;
    }

    public SentinelLog setPassQps(String passQps) {
        this.passQps = passQps;
        return this;
    }

    public String getBlockQps() {
        return blockQps;
    }

    public SentinelLog setBlockQps(String blockQps) {
        this.blockQps = blockQps;
        return this;
    }

    public String getFinishQps() {
        return finishQps;
    }

    public SentinelLog setFinishQps(String finishQps) {
        this.finishQps = finishQps;
        return this;
    }

    public String getExceptionQps() { return exceptionQps; }

    public SentinelLog setExceptionQps(String exceptionQps) {
        this.exceptionQps = exceptionQps;
        return this;
    }

    public String getAvgRt() {
        return avgRt;
    }

    public SentinelLog setAvgRt(String avgRt) {
        this.avgRt = avgRt;
        return this;
    }
}

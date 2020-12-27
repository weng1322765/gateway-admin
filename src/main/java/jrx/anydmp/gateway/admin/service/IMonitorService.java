package jrx.anydmp.gateway.admin.service;

import jrx.anydmp.gateway.admin.dto.SentinelLog;
import jrx.anytxn.common.exception.TxnException;

import java.util.List;

public interface IMonitorService {

    /**
     * 通过行为日志检查是否有触发限流，如果有，记录预警信息
     * @param serverId 实例id
     * @param instanceHomeUrl 示例地址
     * @param startTime 开始时间
     * @param endTime 截至时间
     */
    void checkForLimit(String serverId, String instanceHomeUrl, String startTime, String endTime);

    /**
     * 获取实例下面最新的路由流量日志
     * @param instanceId  实例ID
     * @param routeId 路由ID
     * @param startTime  开始时间  Long格式的毫秒时间字符串
     * @param endTime  结束时间  Long格式的毫秒时间字符串
     * @param collectTime  时间毫秒  默认是30000 30秒  最小为1000 一秒
     * @return
     */
    List<SentinelLog> getLastedRouteSentinelLogList(Long instanceId, String routeId, String startTime, String endTime, int collectTime) throws TxnException;
}

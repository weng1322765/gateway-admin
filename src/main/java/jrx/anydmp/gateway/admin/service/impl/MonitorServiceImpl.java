package jrx.anydmp.gateway.admin.service.impl;

import com.alibaba.fastjson.JSON;
import jrx.anydmp.gateway.admin.dto.SentinelLog;
import jrx.anydmp.gateway.admin.enums.InstanceStatus;
import jrx.anydmp.gateway.admin.service.IInstanceInfoService;
import jrx.anydmp.gateway.admin.service.IMonitorService;
import jrx.anydmp.gateway.admin.utils.DateUtils;
import jrx.anydmp.gateway.dto.InstanceInfoDto;
import jrx.anydmp.gateway.entity.InstanceInfo;
import jrx.anydmp.gateway.entity.WarningEvent;
import jrx.anydmp.gateway.mapper.WarningEventMapper;
import jrx.anytxn.common.exception.TxnException;
import jrx.anytxn.common.exception.TxnNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class MonitorServiceImpl implements IMonitorService {
    private static final Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);

    private static String timeFlag = null;//时间标志位


    @Autowired
    private IInstanceInfoService instanceInfoService;

    @Autowired
    WarningEventMapper warningEventMapper;

    @Override
    public void checkForLimit(String serverId, String instanceHomeUrl, String startTime, String endTime) {
        String url = instanceHomeUrl + "/rule/getLogDetail?startTime=" + startTime + "&endTime=" + endTime+"&resource=";
        logger.debug("check limit url:{}",url);
        String result = null;
        try{
            RestTemplate restTemplate = new RestTemplate();
            result = restTemplate.getForObject(url, String.class);
        }catch (Exception e){
            logger.error("获取限流日志错误 url:"+url,e);
        }

        if (StringUtils.isEmpty(result) || "[]".equals(result)) {
            return;
        }
        List<SentinelLog> logList = JSON.parseArray(result, SentinelLog.class);
        logList = logList.stream().filter(r -> Integer.valueOf(r.getBlockQps() == null ? "0" : r.getBlockQps()) > 0).collect(Collectors.toList());
        if (logList.size() == 0) {
            return;
        }
        List<WarningEvent> warningEventList = logList.stream().map(w -> {
            WarningEvent warningEvent = new WarningEvent();
            warningEvent.setEventDatetime(stringToDate(w.getDatetime()));
            warningEvent.setResource(w.getResource());
            warningEvent.setEventType("flow");
            warningEvent.setInstanceName(instanceHomeUrl);
            warningEvent.setServerId(serverId);
            warningEvent.setPassQps(Integer.valueOf(w.getPassQps()));
            warningEvent.setBlockQps(Integer.valueOf(w.getBlockQps()));
            warningEvent.setFinishQps(Integer.valueOf(w.getFinishQps()));
            warningEvent.setAvgRt(Integer.valueOf(w.getAvgRt()));
            return warningEvent;
        }).collect(Collectors.toList());
        warningEventMapper.insertList(warningEventList);
    }

    /**
     * 获取实例下面最新的路由流量日志
     *
     * @param instanceId 实例ID
     * @param routeId    路由ID
     * @param startTime  开始时间  Long格式的毫秒时间字符串
     * @param endTime    结束时间  Long格式的毫秒时间字符串
     * @param collectTime  时间毫秒  默认是30000 30秒  最小为1000 一秒
     * @return
     */
    @Override
    public List<SentinelLog> getLastedRouteSentinelLogList(Long instanceId, String routeId, String startTime, String endTime, int collectTime) throws TxnException {
        InstanceInfoDto instanceInfo = instanceInfoService.getById(instanceId);
        if(instanceInfo==null || !instanceInfo.getStatus().equalsIgnoreCase(InstanceStatus.UP.name())){
            throw new TxnNotFoundException("服务实例不存在或是已经下线");
        }
        String url = "http://" + instanceInfo.getIp() + ":" + instanceInfo.getPort() + "/rule/getLogDetail?startTime=" + startTime + "&endTime=" + endTime;
        if (!StringUtils.isEmpty(routeId)) {
            url += "&resource=" + routeId;
        }
        List<SentinelLog> resultList = new ArrayList();
        try{
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(url, String.class);
            if (!StringUtils.isEmpty(result) || !"[]".equals(result)) {
                resultList = JSON.parseArray(result, SentinelLog.class);
            }
        }catch (Exception e){
            logger.error("获取路由流量日志错误 url:"+url,e);
        }
        return collectSentinelLogByTime(resultList, collectTime);

    }

    /**
     * 根据时间汇总日志
     * @param list
     * @param collectTime  时间毫秒  默认是30000 30秒  最小为1000 一秒
     * @return
     */
    private List<SentinelLog> collectSentinelLogByTime(List<SentinelLog> list,  int collectTime) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        //
        if(collectTime<1000){
            collectTime = 30000;
        }
        final int _collectTime = collectTime;

        List<SentinelLog> tmpList = new ArrayList();
        //时间按30秒汇总 0-29改成0秒   31-59改成30
        list.stream().forEach((v) -> {
            Long d = DateUtils.DatetimeParseToLong(v.getDatetime(),"YYYY-MM-dd HH:mm:ss");
            d = d - (d%_collectTime);
            v.setDatetime(DateUtils.parseToDatetime(d,"YYYY-MM-dd HH:mm:ss"));
        });

        list.stream().collect(Collectors.groupingBy(SentinelLog::getDatetime)).forEach((k, v) -> {
                    Optional<SentinelLog> op = v.stream().reduce((v1, v2) -> {
                        v1.setPassQps(String.valueOf(Integer.valueOf(v1.getPassQps()) + Integer.valueOf(v2.getPassQps())));
                        v1.setBlockQps(String.valueOf(Integer.valueOf(v1.getBlockQps()) + Integer.valueOf(v2.getBlockQps())));
                        v1.setFinishQps(String.valueOf(Integer.valueOf(v1.getFinishQps()) + Integer.valueOf(v2.getFinishQps())));
                        tmpList.add(v1);
                        return v1;
                    });
                    tmpList.add(op.orElse(new SentinelLog()));
                }
        );
        return tmpList.stream().distinct().collect(Collectors.toList());
    }

    private Date stringToDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(date);
        } catch (ParseException e) {
            logger.warn("字符串{}转换日期类型失败！", date);
            return null;
        }
    }

    @Scheduled(initialDelay = 60 * 1000L, fixedDelay = 60 * 1000L)
    public void timeTask() {
        logger.debug("==============限流预警信息定时任务开始=============");
        try {
            flowTimerTask();
        } catch (Exception e) {
            logger.error("*****限流预警信息定时任务异常*****", e);
        }
        logger.debug("==============限流预警信息定时任务结束============");
    }



    private void flowTimerTask() throws Exception{
        final String startTime;
        final String endTime;
        if (timeFlag == null) {
            endTime = String.valueOf(System.currentTimeMillis());
            startTime = String.valueOf(System.currentTimeMillis() - 300000L);
        } else {
            startTime = timeFlag;
            endTime = String.valueOf(Long.valueOf(startTime) + 300000L);
        }
        List<InstanceInfo> instanceInfoList = instanceInfoService.getUpInstanceList();
//        List<InstanceInfo> instanceInfoList = test();
        ExecutorService exe = Executors.newCachedThreadPool();
        CountDownLatch countDownLatch = new CountDownLatch(instanceInfoList.size());
        for (InstanceInfo instance : instanceInfoList) {
            exe.execute(() -> {
                String url = "http://" + instance.getIp() + ":" + instance.getPort();
                try{
                    this.checkForLimit(instance.getServerId(), url, startTime, endTime);
                }catch (Exception e){
                    logger.error("检查限流错误：",e);
                }

                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        exe.shutdown();
        timeFlag = endTime;
    }


}

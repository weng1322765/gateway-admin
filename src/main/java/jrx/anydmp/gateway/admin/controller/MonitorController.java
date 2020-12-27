package jrx.anydmp.gateway.admin.controller;

import io.swagger.annotations.Api;
import jrx.anydmp.gateway.admin.service.IMonitorService;
import jrx.anytxn.common.data.TxnRespResult;
import jrx.anytxn.common.exception.TxnArgumentException;
import jrx.anytxn.common.exception.TxnException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.regex.Pattern;

@Api(description = "监控信息")
@RestController
@RequestMapping(value = "/monitor")
public class MonitorController {

    @Autowired
    private IMonitorService monitorService;


    @GetMapping("/list")
    public TxnRespResult getCountInfo(@RequestParam Long instanceId,
                                      @RequestParam String startTime,
                                      @RequestParam String endTime,
                                      @RequestParam(required = false) String routeId) throws TxnException {
        if (!Pattern.matches("[0-9]{13}", startTime) || !Pattern.matches("[0-9]{13}", endTime)
                || Long.parseLong(endTime) < Long.parseLong(startTime)) {
            throw new TxnArgumentException("日期格式填写错误！");
        }

        return new TxnRespResult().getSuccess((ArrayList)monitorService.getLastedRouteSentinelLogList(instanceId,routeId,startTime,endTime,10000));
    }


}

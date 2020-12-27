package jrx.anydmp.gateway.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jrx.anydmp.gateway.admin.dto.OperateResult;
import jrx.anydmp.gateway.admin.enums.Status;
import jrx.anydmp.gateway.admin.service.IFlowLimitService;
import jrx.anydmp.gateway.admin.service.IInstanceInfoService;
import jrx.anydmp.gateway.admin.service.IOperateService;
import jrx.anydmp.gateway.admin.service.IRouteInfoService;
import jrx.anydmp.gateway.dto.FlowLimitDto;
import jrx.anydmp.gateway.dto.InstanceInfoDto;
import jrx.anydmp.gateway.dto.RouteInfoDto;
import jrx.anydmp.gateway.entity.OperateEvent;
import jrx.anytxn.common.data.TxnPage;
import jrx.anytxn.common.data.TxnRespCode;
import jrx.anytxn.common.data.TxnRespResult;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 限流控制 api
 *
 * @author zhao tingting
 * @date 2018/10/25
 */
@Api(description = "流控信息")
@RestController
@RequestMapping(value = "/flowLimit")
public class FlowLimitController {
    private static final Logger log = LoggerFactory.getLogger(FlowLimitController.class);

    @Autowired
    private IFlowLimitService flowLimitService;

    @Autowired
    private IOperateService operateService;

    @Autowired
    private IInstanceInfoService instanceInfoService;

    @Autowired
    private IRouteInfoService routeInfoService;

    /**
     * 添加限流信息
     * @param flowLimitDto 限流信息对象
     * @return
     */
    @PostMapping
    @ApiOperation(value = "添加限流信息")
    TxnRespResult<FlowLimitDto> create(@Valid @RequestBody FlowLimitDto flowLimitDto) {
        TxnRespResult result = new TxnRespResult();
        FlowLimitDto dto = flowLimitService.add(flowLimitDto);
        return result.getSuccess(dto);
    }


    /**
     * 删除限流信息
     * @param  id 主键id
     * @return
     *
     */
    @ApiOperation(value = "删除限流信息")
    @PutMapping("/id/{id}")
    TxnRespResult<Boolean> delete(@PathVariable Integer id) {
        TxnRespResult result = new TxnRespResult();
        FlowLimitDto flowLimitDto = flowLimitService.remove(id);
//        String msg = activeGateway(flowLimitDto, "delete");
        return result.getSuccess(flowLimitDto);
    }

    /**
     * 编辑限流信息
     * @param flowLimitDto 限流信息对象
     * @return
     *
     */
    @ApiOperation(value = "编辑限流信息")
    @PutMapping("/update")
    TxnRespResult<FlowLimitDto> update(@Valid @RequestBody FlowLimitDto flowLimitDto) {
        TxnRespResult result = new TxnRespResult();
        FlowLimitDto infoDto = flowLimitService.modify(flowLimitDto);
        String msg = "成功！";
        if (infoDto.getStatus() == Status.ENABLE.getCode()) {
            msg = activeGateway(infoDto, "update");
        }
        return result.getSuccess(msg, infoDto);
    }

    /**
     * 根据serverId获取限流信息
     * @param serverId 服务id
     * @return
     *
     */
    @ApiOperation(value = "根据serverId获取限流信息")
    @GetMapping("/serverId/{serverId}")
    TxnRespResult<ArrayList<FlowLimitDto>> getByServerId(@PathVariable String serverId) {
        TxnRespResult result = new TxnRespResult();
        ArrayList<FlowLimitDto> infoDtoList = (ArrayList)flowLimitService.getByServerId(serverId);
        return result.getSuccess(infoDtoList);
    }

    /**
     * 根据id获取限流信息
     * @param id 主键id
     * @return FlowLimitDto
     *
     */
    @ApiOperation(value = "根据id获取限流信息")
    @GetMapping("/id/{id}")
    TxnRespResult<FlowLimitDto> getById(@PathVariable Integer id) {
        TxnRespResult result = new TxnRespResult();
        FlowLimitDto infoDto = flowLimitService.getById(id);
        return result.getSuccess(infoDto);
    }

    /**
     * 分页查询限流控制信息
     * @param pageNum 每页条数
     * @param pageSize 页码
     * @return FlowLimitDto
     *
     */
    @ApiOperation(value = "分页查询限流控制信息")
    @GetMapping("/page")
    TxnRespResult<TxnPage<FlowLimitDto>> getAllPage(@RequestParam(defaultValue = "1", required = false) Integer pageNum,
                                                                    @RequestParam(defaultValue = "10", required = false) Integer pageSize,
                                                                    @RequestParam(required = false) String serverId,
                                                                    @RequestParam(required = false) Integer status) {
        TxnRespResult result = new TxnRespResult();
        TxnPage<FlowLimitDto> page = flowLimitService.getPageByServerIdAndStatus(pageNum, pageSize, serverId, status);
        return result.getSuccess(page);
    }

	/**
	 * 根据ID修改限流状态
	 * @param id ID
	 * @param status 状态
	 * @return
	 *
	 */
    @ApiOperation(value = "根据ID修改限流状态")
    @PutMapping("/id/{id}/status/{status}")
    TxnRespResult<Boolean> updateByStatus(@PathVariable Integer id, @PathVariable Integer status) {
        TxnRespResult result = new TxnRespResult();
        String msg = null;
        FlowLimitDto dto = flowLimitService.modifyByStauts(id, status);
        if (status == 0) {
            msg = activeGateway(dto, "delete");
        }
        if (status == 1) {
            msg = activeGateway(dto, "new");
        }
        return result.getSuccess(msg, null);
    }

    private String activeGateway(FlowLimitDto flowLimitDto, String operateType) {
	    try {
            OperateResult result = operateService.formatOperateEvent(new OperateEvent()
                    .setServerId(flowLimitDto.getServerId())
                    .setType("flow")
                    .setOperateType(operateType)
                    .setObjectId(flowLimitDto.getId())
            );
            if (result == null) {
                return "该服务" + flowLimitDto.getServerId() + "没有可用实例";
            }
            result = operateService.informGateway(result.getOperateEventList(), result);
            return "更新" + result.getInstance() + "个实例，"
                    + result.getSuccess() + "个生效成功，" + result.getFailed() + "个生效失败！";
        }catch (Exception e){
            log.error("系统异常", e);
            return "数据保存成功，但同步网关应用失败！";
        }
    }



    /**
     * 查询某个实例内存中正在使用的限流信息
     * @return
     */
    @GetMapping("/list")
    public TxnRespResult<TxnPage<FlowLimitDto>> queryFlowList(@RequestParam Long instanceId,
                                                              @RequestParam(defaultValue = "1", required = false) Integer pageNum,
                                                              @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        TxnRespResult respResult = new TxnRespResult();
        try {
            InstanceInfoDto instanceInfoDto = instanceInfoService.getById(instanceId);
            String url = "http://" + instanceInfoDto.getIp() + ":" + instanceInfoDto.getPort() + "/rule/getRules?type=flow";
//        String url = "http://localhost:9202/rule/getRules?type=flow";
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(url, String.class);
            List<FlowLimitDto> flowLimitDtoList = queryFlowListOnline(result, instanceInfoDto.getServerId());
            int fromIndex = Integer.valueOf(pageSize) * (Integer.valueOf(pageNum) - 1);
            int toIndex = fromIndex + Integer.valueOf(pageSize);
            int size = flowLimitDtoList.size();
            if (toIndex >= size) {
                toIndex = size;
            }
            List<FlowLimitDto> list =  flowLimitDtoList.subList(fromIndex, toIndex);
            TxnPage<FlowLimitDto> routeInfoDtoTxnPage = new TxnPage(pageNum, pageSize, size, list);
            return respResult.getSuccess(routeInfoDtoTxnPage);
        }catch (Exception e){
            return respResult.getFail(TxnRespCode.ERROR.getCode(), e);
        }
    }

    private List<FlowLimitDto> queryFlowListOnline(String result, String serverId) {
        List<FlowLimitDto> flowLimitList = new ArrayList<>();
        if (StringUtils.isEmpty(result) || "[]".equals(result)) {
            return flowLimitList;
        }

        List<FlowLimitDto> dbList = flowLimitService.getByServerId(serverId);
        JSONArray jsonArray = JSON.parseArray(result);
        for (int i = 0; i < jsonArray.size(); i++) {
            FlowLimitDto flowLimit = new FlowLimitDto();
            String resource = jsonArray.getJSONObject(i).getString("resource");
            if (resource.startsWith("/")) {
                flowLimit.setUrl(resource);
            } else if (Pattern.matches("[0-9]+", resource)) {
                flowLimit.setRouteId(Integer.valueOf(resource));
            }
            flowLimit.setServerId(serverId);
            flowLimit.setThresholdType(jsonArray.getJSONObject(i).getIntValue("grade"));
            flowLimit.setSingleThreshold(jsonArray.getJSONObject(i).getIntValue("count"));
            flowLimit.setFlowControlMethod(jsonArray.getJSONObject(i).getIntValue("controlBehavior"));
            flowLimit.setFlowControlMode(jsonArray.getJSONObject(i).getIntValue("strategy"));
            Optional<FlowLimitDto> dbFlowLimitOpt = dbList.stream().filter(f -> {
                if (flowLimit.getRouteId() != null) {
                    return flowLimit.getRouteId().equals(f.getRouteId());
                } else {
                    return flowLimit.getUrl().equals(f.getUrl());
                }
            }).findFirst();
            if(dbFlowLimitOpt.isPresent()){
                flowLimit.setName(dbFlowLimitOpt.get().getName());
                flowLimit.setServerName(dbFlowLimitOpt.get().getServerName());
                flowLimit.setId(dbFlowLimitOpt.get().getId());
            }

            if (flowLimit.getRouteId() != null) {
                try{
                    RouteInfoDto routeInfoDto = routeInfoService.getById(flowLimit.getRouteId());
                    flowLimit.setRouteName(routeInfoDto!=null?routeInfoDto.getRouteName():"");
                }catch (Exception e){
                    log.error("获取路由名称错误："+flowLimit.getRouteId(),e);
                }

            }

            flowLimitList.add(flowLimit);
        }

        return flowLimitList;
    }






}


package jrx.anydmp.gateway.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jrx.anydmp.gateway.admin.dto.OperateResult;
import jrx.anydmp.gateway.admin.enums.Status;
import jrx.anydmp.gateway.admin.service.IDegradeService;
import jrx.anydmp.gateway.admin.service.IInstanceInfoService;
import jrx.anydmp.gateway.admin.service.IOperateService;
import jrx.anydmp.gateway.admin.service.IRouteInfoService;
import jrx.anydmp.gateway.dto.DegradeDto;
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
import java.util.regex.Pattern;

/**
 * 降级控制 api
 *
 * @author zhao tingting
 * @date 2018/10/25
 */
@Api(description = "降级控制")
@RestController
@RequestMapping(value = "/degrade")
public class DegradeController {
    private static final Logger log = LoggerFactory.getLogger(DegradeController.class);
    
    @Autowired
    private IDegradeService degradeService;

    @Autowired
    private IOperateService operateService;

    @Autowired
    private IInstanceInfoService instanceInfoService;

    @Autowired
    private IRouteInfoService routeInfoService;

    /**
     * 添加降级信息
     * @param degradeDto 降级信息对象
     * @return
     */
    @PostMapping
    @ApiOperation(value = "添加降级信息")
    TxnRespResult<DegradeDto> create(@Valid @RequestBody DegradeDto degradeDto) {
        TxnRespResult result = new TxnRespResult();
        DegradeDto dto = degradeService.add(degradeDto);
        return result.getSuccess(dto);
    }


    /**
     * 删除降级信息
     * @param  id 主键id
     * @return
     *
     */
    @ApiOperation(value = "删除降级信息")
    @PutMapping("/id/{id}")
    TxnRespResult<Boolean> delete(@PathVariable Integer id) {
        TxnRespResult result = new TxnRespResult();
        DegradeDto degradeDto = degradeService.remove(id);
//        String msg = activeGateway(degradeDto, "delete");
        return result.getSuccess(degradeDto);
    }

    /**
     * 编辑降级信息
     * @param degradeDto 降级信息对象
     * @return
     *
     */
    @ApiOperation(value = "编辑降级信息")
    @PutMapping("/update")
    TxnRespResult<DegradeDto> update(@Valid @RequestBody DegradeDto degradeDto) {
        TxnRespResult result = new TxnRespResult();
        DegradeDto infoDto = degradeService.modify(degradeDto);
        String msg = "成功！";
        if (infoDto.getStatus() == Status.ENABLE.getCode()) {
            msg = activeGateway(infoDto, "update");
        }
        return result.getSuccess(msg, infoDto);
    }

    /**
     * 根据id获取降级信息
     * @param id 主键id
     * @return DegradeDto
     *
     */
    @ApiOperation(value = "根据id获取降级信息")
    @GetMapping("/id/{id}")
    TxnRespResult<DegradeDto> getById(@PathVariable Integer id) {
        TxnRespResult result = new TxnRespResult();
        DegradeDto infoDto = degradeService.getById(id);
        return result.getSuccess(infoDto);
    }

    /**
     * 分页查询降级控制信息
     * @param pageNum 每页条数
     * @param pageSize 页码
     * @return DegradeDto
     *
     */
    @ApiOperation(value = "分页查询降级控制信息")
    @GetMapping("/page")
    TxnRespResult<TxnPage<DegradeDto>> getAllPage(@RequestParam(defaultValue = "1", required = false) Integer pageNum,
                                                  @RequestParam(defaultValue = "10", required = false) Integer pageSize,
                                                  @RequestParam(required = false) String serverId,
                                                  @RequestParam(required = false) Integer status) {
        TxnRespResult result = new TxnRespResult();
        TxnPage<DegradeDto> page = degradeService.getPageByServerIdAndStatus(pageNum, pageSize, serverId, status);
        return result.getSuccess(page);
    }

    /**
     * 根据ID修改降级状态
     * @param id ID
     * @param status 状态
     * @return
     *
     */
    @ApiOperation(value = "根据ID修改降级状态")
    @PutMapping("/id/{id}/status/{status}")
    TxnRespResult<Boolean> updateByStatus(@PathVariable Integer id, @PathVariable Integer status) {
        TxnRespResult result = new TxnRespResult();
        DegradeDto dto = degradeService.modifyByStauts(id, status);
        String msg = null;
        if (status == 0) {
            msg = activeGateway(dto, "delete");
        }
        if (status == 1) {
            msg = activeGateway(dto, "new");
        }
        return result.getSuccess(msg, null);
    }


    private String activeGateway(DegradeDto degradeDto, String operateType) {
        try {
            OperateResult result = operateService.formatOperateEvent(new OperateEvent()
                    .setServerId(degradeDto.getServerId())
                    .setType("degrade")
                    .setOperateType(operateType)
                    .setObjectId(degradeDto.getId())
            );
            if (result == null) {
                return "该服务" + degradeDto.getServerId() + "没有可用实例";
            }
            result = operateService.informGateway(result.getOperateEventList(), result);
            return "更新" + result.getInstance() + "个实例，"
                    + result.getSuccess() + "个生效成功，" + result.getFailed() + "个生效失败！";
        } catch (Exception e) {
            log.error("系统异常", e);
            return "数据保存成功，但同步网关应用失败！";
        }
    }



    /**
     * 查询某个实例内存中正在使用的降级信息
     * @return
     */
    @GetMapping("/list")
    public TxnRespResult<TxnPage<DegradeDto>> queryDegradeList(@RequestParam Long instanceId,
                                                              @RequestParam(defaultValue = "1", required = false) Integer pageNum,
                                                              @RequestParam(defaultValue = "10", required = false) Integer pageSize) {

        TxnRespResult respResult = new TxnRespResult();
        try {
            InstanceInfoDto instanceInfoDto = instanceInfoService.getById(instanceId);
            String url = "http://" + instanceInfoDto.getIp() + ":" + instanceInfoDto.getPort() + "/rule/getRules?type=degrade";
//        String url = "http://localhost:9202/rule/getRules?type=degrade";
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(url, String.class);
            List<DegradeDto> degradeDtoList = queryDegradeListOnline(result, instanceInfoDto.getServerId());

            int fromIndex = Integer.valueOf(pageSize) * (Integer.valueOf(pageNum) - 1);
            int toIndex = fromIndex + Integer.valueOf(pageSize);
            int size = degradeDtoList.size();
            if (toIndex >= size) {
                toIndex = size;
            }
            List<DegradeDto> list =  degradeDtoList.subList(fromIndex, toIndex);
            TxnPage<DegradeDto> degradeDtoTxnPage = new TxnPage(pageNum, pageSize, size, list);
            return respResult.getSuccess(degradeDtoTxnPage);
        }catch (Exception e){
            return respResult.getFail(TxnRespCode.ERROR.getCode(), e);
        }
    }

    private List<DegradeDto> queryDegradeListOnline(String result, String serverId) {
        List<DegradeDto> degradeList = new ArrayList<>();
        if (StringUtils.isEmpty(result) || "[]".equals(result)) {
            return degradeList;
        }

        List<DegradeDto> dbList = degradeService.getByServerId(serverId);
        JSONArray jsonArray = JSON.parseArray(result);
        for (int i = 0; i < jsonArray.size(); i++) {
            DegradeDto degrade = new DegradeDto();
            String resource = jsonArray.getJSONObject(i).getString("resource");
            if (resource.startsWith("/")) {
                degrade.setUrl(resource);
            } else if (Pattern.matches("[0-9]+", resource)) {
                degrade.setRouteId(Integer.valueOf(resource));
            }
            degrade.setServerId(serverId);
            degrade.setThresholdType(jsonArray.getJSONObject(i).getIntValue("grade"));
            degrade.setThresholdValue(jsonArray.getJSONObject(i).getDoubleValue("count"));
            degrade.setPeriod(jsonArray.getJSONObject(i).getIntValue("timeWindow"));
            DegradeDto dbDegrade = dbList.stream().filter(f -> {
                if (degrade.getRouteId() != null) {
                    return degrade.getRouteId().equals(f.getRouteId());
                } else {
                    return degrade.getUrl().equals(f.getUrl());
                }
            }).findFirst().get();
            degrade.setName(dbDegrade.getName());
            degrade.setServerName(dbDegrade.getServerName());
            degrade.setId(dbDegrade.getId());
			if (degrade.getRouteId() != null) {
				RouteInfoDto routeInfoDto = routeInfoService.getById(degrade.getRouteId());
				degrade.setRouteName(routeInfoDto.getRouteName());
			}
            degradeList.add(degrade);
        }
        return degradeList;
    }


}

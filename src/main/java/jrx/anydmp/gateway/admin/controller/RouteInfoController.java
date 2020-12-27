package jrx.anydmp.gateway.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jrx.anydmp.gateway.admin.dto.CommonProperty;
import jrx.anydmp.gateway.admin.dto.OperateResult;
import jrx.anydmp.gateway.admin.enums.Status;
import jrx.anydmp.gateway.admin.service.ICommonService;
import jrx.anydmp.gateway.admin.service.IInstanceInfoService;
import jrx.anydmp.gateway.admin.service.IOperateService;
import jrx.anydmp.gateway.admin.service.IRouteInfoService;
import jrx.anydmp.gateway.admin.utils.DateUtils;
import jrx.anydmp.gateway.dto.InstanceInfoDto;
import jrx.anydmp.gateway.dto.RouteInfoDto;
import jrx.anydmp.gateway.dto.RoutePredicateDto;
import jrx.anydmp.gateway.dto.RouteRuleDto;
import jrx.anydmp.gateway.entity.OperateEvent;
import jrx.anytxn.common.data.TxnPage;
import jrx.anytxn.common.data.TxnRespCode;
import jrx.anytxn.common.data.TxnRespResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 路由信息 api
 *
 * @author zhao tingting
 * @date 2018/10/25
 */
@Api(description = "路由信息")
@RestController
@RequestMapping(value = "/routeInfo")
public class RouteInfoController {
    private static final Logger log = LoggerFactory.getLogger(RouteInfoController.class);

    public static final String PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private IRouteInfoService routeInfoService;

    @Autowired
    private IOperateService operateService;

    @Autowired
    private IInstanceInfoService instanceInfoService;

    @Autowired
    private ICommonService commonService;

    public List<RoutePredicateDto> spiltRules(RouteInfoDto routeInfoDto) {
        List<RoutePredicateDto> list = new ArrayList<>();
        List<RouteRuleDto> ruleDtoList = routeInfoDto.getRuleDtoList();
		if (!StringUtils.isEmpty(routeInfoDto.getInterfaceUrl())) {
			RoutePredicateDto routePath = new RoutePredicateDto();
			routePath.setType(RoutePredicateDto.PATH);
			routePath.setPattern(routeInfoDto.getInterfaceUrl());
			list.add(routePath);
		}
		if (ruleDtoList != null && ruleDtoList.size() != 0) {
			for (RouteRuleDto ruleDto : ruleDtoList) {
                RoutePredicateDto route = new RoutePredicateDto();
                if (ruleDto.getRuleType() != null) {
                    if (ruleDto.getRuleType().equals(RoutePredicateDto.QUERY)) {
                        route.setType(RoutePredicateDto.QUERY);
                        route.setParam(ruleDto.getParam1());
                        route.setRegexp(ruleDto.getParam2());
                    }
                    if (ruleDto.getRuleType().equals(RoutePredicateDto.AFTER) && !StringUtils.isEmpty(ruleDto.getParam1())) {
                        route.setType(RoutePredicateDto.AFTER);
                        route.setStartDatetime(String.valueOf(DateUtils.DatetimeParseToLong(ruleDto.getParam1(), PATTERN)));

                    }
                    if (ruleDto.getRuleType().equals(RoutePredicateDto.BEFORE) && !StringUtils.isEmpty(ruleDto.getParam1())) {
                        route.setType(RoutePredicateDto.BEFORE);
                        route.setEndDatetime(String.valueOf(DateUtils.DatetimeParseToLong(ruleDto.getParam1(), PATTERN)));
                    }
                    if (ruleDto.getRuleType().equals(RoutePredicateDto.BETWEEN) && !StringUtils.isEmpty(ruleDto.getParam1())
                            && !StringUtils.isEmpty(ruleDto.getParam2())) {
                        route.setType(RoutePredicateDto.BETWEEN);
                        route.setStartDatetime(String.valueOf(DateUtils.DatetimeParseToLong(ruleDto.getParam1(), PATTERN)));
                        route.setEndDatetime(String.valueOf(DateUtils.DatetimeParseToLong(ruleDto.getParam2(), PATTERN)));
                    }
                    list.add(route);
                }
			}

		}
        return list;
    }



    /**
     * 添加路由信息
     * @param routeInfoDto 路由信息对象
     * @return
     */
    @PostMapping
    @ApiOperation(value = "添加路由信息")
    TxnRespResult<RouteInfoDto> create(@Valid @RequestBody RouteInfoDto routeInfoDto) {
        TxnRespResult result = new TxnRespResult();
        List<RoutePredicateDto> list = spiltRules(routeInfoDto);
        routeInfoDto.setRules(JSON.toJSONString(list, SerializerFeature.DisableCircularReferenceDetect));
        RouteInfoDto dto = routeInfoService.add(routeInfoDto);
        return result.getSuccess(dto);
    }


    /**
     * 删除路由信息
     * @param  id 主键id
     * @return
     *
     */
    @ApiOperation(value = "删除路由信息")
    @PutMapping("/id/{id}")
    TxnRespResult<Boolean> delete(@PathVariable Integer id) {
        TxnRespResult result = new TxnRespResult();
        RouteInfoDto routeInfoDto = routeInfoService.remove(id);
//        String msg = activeGateway(routeInfoDto, "delete");
        return result.getSuccess(routeInfoDto);
    }

    /**
     * 编辑路由信息
     * @param routeInfoDto 路由信息对象
     * @return
     *
     */
    @ApiOperation(value = "编辑路由信息")
    @PutMapping
    TxnRespResult<RouteInfoDto> update(@Valid @RequestBody RouteInfoDto routeInfoDto) {
        TxnRespResult result = new TxnRespResult();
        List<RoutePredicateDto> list = spiltRules(routeInfoDto);
        routeInfoDto.setRules(JSON.toJSONString(list));
        RouteInfoDto infoDto = routeInfoService.modify(routeInfoDto);
        String msg = "成功！";
        if (infoDto.getStatus().equals(Status.ENABLE.getCode())) {
            msg = activeGateway(infoDto, "update");
        }
        return result.getSuccess(msg, infoDto);
    }

    /**
     * 根据serverId获取路由信息
     * @param serverId 服务id
     * @return
     *
     */
    @ApiOperation(value = "根据serverId获取路由信息")
    @GetMapping("/serverId/{serverId}")
    TxnRespResult<ArrayList<RouteInfoDto>> getByServerId(@PathVariable String serverId) {
        TxnRespResult result = new TxnRespResult();
        ArrayList<RouteInfoDto> infoDtoList = (ArrayList)routeInfoService.getByServerId(serverId);
        return result.getSuccess(infoDtoList);
    }

    /**
     * 根据id获取路由信息
     * @param id 主键id
     * @return RouteInfoDto
     *
     */
    @ApiOperation(value = "根据id获取路由信息")
    @GetMapping("/id/{id}")
    TxnRespResult<RouteInfoDto> getById(@PathVariable Integer id) {
        TxnRespResult result = new TxnRespResult();
        RouteInfoDto infoDto = routeInfoService.getById(id);
        List<RoutePredicateDto> list = null;
        try {
            list = JSON.parseArray(infoDto.getRules(), RoutePredicateDto.class);
        } catch (Exception e) {
            log.debug("路由规则格式不正确");
        }
        List<RouteRuleDto> ruleDtoList = new ArrayList<>();
        if (list != null && list.size() != 0) {
            for (RoutePredicateDto routePredicateDto : list) {
                if(routePredicateDto==null || StringUtils.isEmpty(routePredicateDto.getType())){
                    //update by zwg 20181119 如果对象为空或是类型为空不处理
                    continue;
                }
                RouteRuleDto routeRuleDto = new RouteRuleDto();
                if (routePredicateDto.getType().equals(RoutePredicateDto.PATH)) {
                   continue;
                }
                if (routePredicateDto.getType().equals(RoutePredicateDto.QUERY)) {
                    routeRuleDto.setRuleType(RoutePredicateDto.QUERY);
                    routeRuleDto.setParam1(routePredicateDto.getParam());
                    routeRuleDto.setParam2(routePredicateDto.getRegexp());
                }
                if (routePredicateDto.getType().equals(RoutePredicateDto.AFTER)) {
                    routeRuleDto.setRuleType(RoutePredicateDto.AFTER);
                    routeRuleDto.setParam1(DateUtils.parseToDatetime(Long.parseLong(routePredicateDto.getStartDatetime()), PATTERN));
                }
                if (routePredicateDto.getType().equals(RoutePredicateDto.BEFORE)) {
                    routeRuleDto.setRuleType(RoutePredicateDto.BEFORE);
                    routeRuleDto.setParam1(DateUtils.parseToDatetime(Long.parseLong(routePredicateDto.getEndDatetime()), PATTERN));
                }
                if (routePredicateDto.getType().equals(RoutePredicateDto.BETWEEN)) {
                    routeRuleDto.setRuleType(RoutePredicateDto.BETWEEN);
                    routeRuleDto.setParam1(DateUtils.parseToDatetime(Long.parseLong(routePredicateDto.getStartDatetime()), PATTERN));
                    routeRuleDto.setParam2(DateUtils.parseToDatetime(Long.parseLong(routePredicateDto.getEndDatetime()), PATTERN));
                }
                ruleDtoList.add(routeRuleDto);
            }
        }
        infoDto.setRuleDtoList(ruleDtoList);

        return result.getSuccess(infoDto);
    }


    /**
     * 分页查询路由信息
     * @param serverId 服务Id 全名匹配
     * @param routeName 路由名称支持模糊匹配
     * @param pageNum 每页条数
     * @param pageSize 页码
     * @return RouteInfoDto
     *
     */
    @ApiOperation(value = "分页查询路由信息")
    @GetMapping("/page")
    TxnRespResult<TxnPage<RouteInfoDto>> getAllPage(@RequestParam(defaultValue = "1", required = false) Integer pageNum,
                                                     @RequestParam(defaultValue = "10", required = false) Integer pageSize,
                                                    @RequestParam(required = false) String serverId,
                                                     @RequestParam(required = false) String routeName) {
        TxnRespResult result = new TxnRespResult();
        TxnPage<RouteInfoDto> page = routeInfoService.getPageByServerIdAndRouteName(serverId, routeName, pageNum, pageSize);
        return result.getSuccess(page);
    }

    /**
     * 根据ID修改路由状态
     * @param id ID
     * @param status 状态
     * @return
     *
     */
    @ApiOperation(value = "根据ID修改路由状态")
    @PutMapping("/id/{id}/status/{status}")
    TxnRespResult<Boolean> updateByStatus(@PathVariable Integer id, @PathVariable Integer status) {
        TxnRespResult result = new TxnRespResult();

        // 构造routeInfoDto
        RouteInfoDto dto = routeInfoService.getById(id);
        CommonProperty route = new CommonProperty();
        route.setId(id);
        route.setServerId(dto.getServerId());
        route.setType("route");

        String msg = null;
        if (status == 0) {
            OperateResult deleteResult = commonService.handleGateway(route, "delete");
//            msg = activeGateway(dto, "delete");
            if (deleteResult.getSuccess() > 0) {
                routeInfoService.modifyByStauts(id, status);
            } else {
                // TODO 同步网关应用如果有一个成功便更新状态，否则失败不更新，失败的数据是否保存下来
				return result.getFail(9999,"同步数据失败");


            }
        }
        if (status == 1) {
            OperateResult newResult = commonService.handleGateway(route, "new");
//            msg = activeGateway(dto, "new");
            if (newResult.getSuccess() > 0) {
                routeInfoService.modifyByStauts(id, status);
            } else {
                // TODO 同步网关应用如果有一个成功便更新状态，否则失败不更新，失败的数据是否保存下来
                return result.getFail(9999,"同步数据失败");
            }
        }
        return result.getSuccess(msg, null);
    }


    /**
     * 获取非删除状态的路由ID和路由名称，用于选择的下来菜单
     * @return
     *
     */
    @ApiOperation(value = "获取非删除状态的路由ID和名称")
    @GetMapping("/names/serverId/{serverId}")
    TxnRespResult<ArrayList<RouteInfoDto>> getIdAndNameList(@PathVariable(required = false) String serverId) {
        TxnRespResult result = new TxnRespResult();
        ArrayList<RouteInfoDto> infoDtoList = (ArrayList)routeInfoService.getIdAndNameList(serverId);
        return result.getSuccess(infoDtoList);
    }


    private String activeGateway(RouteInfoDto routeInfoDto, String operateType) {
        try {
            OperateResult result = operateService.formatOperateEvent(new OperateEvent()
                    .setServerId(routeInfoDto.getServerId())
                    .setType("route")
                    .setOperateType(operateType)
                    .setObjectId(routeInfoDto.getId())
            );
            if (result == null) {
                return "该服务" + routeInfoDto.getServerId() + "没有可用实例";
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
     * 查询某个实例内存中正在使用的路由
     * @return
     */
    @GetMapping("/list")
    public TxnRespResult<TxnPage<RouteInfoDto>> queryRouteOnline(@RequestParam Long instanceId,
                                               @RequestParam(defaultValue = "1", required = false) Integer pageNum,
                                               @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
        TxnRespResult respResult = new TxnRespResult();
        try {
            InstanceInfoDto instanceInfoDto = instanceInfoService.getById(instanceId);
            String url = "http://" + instanceInfoDto.getIp() + ":" + instanceInfoDto.getPort() + "/actuator/gateway/routes";
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.getForObject(url, String.class);

            List<RouteInfoDto> routeInfoList = queryRouteListOnline(result, instanceInfoDto.getServerId());
            // 每页的起始索引
            int fromIndex = Integer.valueOf(pageSize) * (Integer.valueOf(pageNum) - 1);
            int toIndex = fromIndex + Integer.valueOf(pageSize);
            int size = routeInfoList.size();
            if (toIndex >= size) {
                toIndex = size;
            }
            List<RouteInfoDto> list =  routeInfoList.subList(fromIndex, toIndex);
            return respResult.getSuccess(new TxnPage(pageNum, pageSize, size, list));
        } catch (Exception e) {
            return respResult.getFail(TxnRespCode.ERROR.getCode(), e);
        }
    }


    private List<RouteInfoDto> queryRouteListOnline(String result, String serverId) {
        List<RouteInfoDto> dbRouteList = routeInfoService.getByServerIdAndStatus(serverId,Status.ENABLE.getCode());
        List<RouteInfoDto> routeInfoList = new ArrayList<>();
        JSONArray jsonArray = JSON.parseArray(result);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (!Pattern.matches("[0-9]+", jsonObject.getString("route_id"))) {
                continue;//排除id不是数字的
            }
            RouteInfoDto routeInfo = new RouteInfoDto();
            routeInfo.setServerId(serverId);
            routeInfo.setId(jsonObject.getInteger("route_id"));
            routeInfo.setRouteUrl(jsonObject.getJSONObject("route_definition").getString("uri"));
            JSONArray predicates = jsonObject.getJSONObject("route_definition").getJSONArray("predicates");
            routeInfo.setRules(formatRules(predicates));
            Optional<RouteInfoDto> optional = dbRouteList.stream().filter(r -> r.getId().intValue() == routeInfo.getId().intValue()).findFirst();
            RouteInfoDto dbRouteInfo;
            if (optional.isPresent()) {
                dbRouteInfo = optional.get();
                routeInfo.setRouteName(dbRouteInfo.getRouteName());
                routeInfo.setServerName(dbRouteInfo.getServerName());
                routeInfo.setInterfaceUrl(dbRouteInfo.getInterfaceUrl());
            } else {
                log.warn("该路由信息{}在数据库中不存在",routeInfo.getId());
            }
            routeInfoList.add(routeInfo);
        }
        return routeInfoList;
    }

    private String formatRules(JSONArray predicates) {
        List<RoutePredicateDto> predicateList = new ArrayList<>();
        for (int j = 0; j < predicates.size(); j++) {
            JSONObject predicate = predicates.getJSONObject(j);
            RoutePredicateDto predicateDTO = new RoutePredicateDto();
            predicateDTO.setType(predicate.getString("name"));
            if (RoutePredicateDto.PATH.equals(predicateDTO.getType())) {
                predicateDTO.setPattern(predicate.getJSONObject("args").getString("pattern"));
            } else if (RoutePredicateDto.QUERY.equals(predicateDTO.getType())) {
                predicateDTO.setParam(predicate.getJSONObject("args").getString("param"));
                predicateDTO.setRegexp(predicate.getJSONObject("args").getString("regexp"));
            } else if (RoutePredicateDto.BEFORE.equals(predicateDTO.getType())) {
                predicateDTO.setEndDatetime(predicate.getJSONObject("args").getString("datetime"));
            } else if (RoutePredicateDto.AFTER.equals(predicateDTO.getType())) {
                predicateDTO.setStartDatetime(predicate.getJSONObject("args").getString("datetime"));
            } else if (RoutePredicateDto.BETWEEN.equals(predicateDTO.getType())) {
                predicateDTO.setStartDatetime(predicate.getJSONObject("args").getString("datetime1"));
                predicateDTO.setEndDatetime(predicate.getJSONObject("args").getString("datetime2"));
            }
            predicateList.add(predicateDTO);
        }
        return JSON.toJSONString(predicateList);
    }

    /**
     * 是否可以删除路由
     *
     * @param id 主键id
     * @return Boolean
     */
    @ApiOperation(value = "是否可以删除路由")
    @GetMapping("/isDelete/id/{id}")
    TxnRespResult<Boolean> isDelete(@PathVariable Integer id) {
        TxnRespResult result = new TxnRespResult();
        Boolean flag = routeInfoService.isDelete(id);
        return result.getSuccess(flag);
    }

}

package jrx.anydmp.gateway.admin.controller;

import com.alibaba.fastjson.JSON;
import com.netflix.appinfo.InstanceInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jrx.anydmp.gateway.admin.service.IInstanceInfoService;
import jrx.anydmp.gateway.dto.InstanceInfoDto;
import jrx.anytxn.common.data.TxnPage;
import jrx.anytxn.common.data.TxnRespResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 实例信息 api
 *
 * @author zhao tingting
 * @date 2018/10/25
 */
@Api(description = "实例信息")
@RestController
@RequestMapping(value = "/instanceInfo")
public class InstanceInfoController {

    @Autowired
    private IInstanceInfoService instanceInfoService;

    /**
     * 添加实例信息
     * @param instanceInfoDto 实例信息对象
     * @return
     */
    @PostMapping
    @ApiOperation(value = "添加实例信息")
    TxnRespResult<InstanceInfoDto> create(@Valid @RequestBody InstanceInfoDto instanceInfoDto) {
        TxnRespResult result = new TxnRespResult();
        InstanceInfoDto dto = instanceInfoService.add(instanceInfoDto);
        return result.getSuccess(dto);
    }


    /**
     * 删除实例信息
     * @param  id 主键id
     * @return
     *
     */
    @ApiOperation(value = "删除实例信息")
    @DeleteMapping("/id/{id}")
    TxnRespResult<Boolean> delete(@PathVariable Long id) {
        TxnRespResult result = new TxnRespResult();
        Boolean flag = instanceInfoService.remove(id);
        return result.getSuccess(flag);
    }

    /**
     * 编辑实例信息
     * @param instanceInfoDto 实例信息对象
     * @return
     *
     */
    @ApiOperation(value = "编辑实例信息")
    @PutMapping("/update")
    TxnRespResult<InstanceInfoDto> update(@Valid @RequestBody InstanceInfoDto instanceInfoDto) {
        TxnRespResult result = new TxnRespResult();
        InstanceInfoDto infoDto = instanceInfoService.modify(instanceInfoDto);
        return result.getSuccess(infoDto);
    }

    /**
     * 根据serverId分页查询实例信息
     * @param pageNum 每页条数
     * @param pageSize 页码
     * @param serverId 服务id
     * @return
     *
     */
    @ApiOperation(value = "根据serverId获取实例信息")
    @GetMapping("/serverId/{serverId}")
    TxnRespResult<TxnPage<InstanceInfoDto>> getByServerId(@RequestParam(defaultValue = "1", required = false) Integer pageNum,
                                                          @RequestParam(defaultValue = "10", required = false) Integer pageSize,
                                                          @PathVariable String serverId) {
        TxnRespResult result = new TxnRespResult();
        TxnPage<InstanceInfoDto> infoDtoList = instanceInfoService.getByServerId(pageNum, pageSize, serverId);
        return result.getSuccess(infoDtoList);
    }

    /**
     * 根据id获取实例信息
     * @param id 主键id
     * @return InstanceInfoDto
     *
     */
    @ApiOperation(value = "根据id获取实例信息")
    @GetMapping("/id/{id}")
    TxnRespResult<InstanceInfoDto> getById(@PathVariable Long id) {
        TxnRespResult result = new TxnRespResult();
        InstanceInfoDto infoDto = instanceInfoService.getById(id);
        return result.getSuccess(infoDto);
    }

    @ApiOperation(value = "获取Eureka实例信息")
    @GetMapping("list")
    TxnRespResult<String> getInstances(@RequestParam String serverId) {
        TxnRespResult result = new TxnRespResult();
        List<InstanceInfo> list = instanceInfoService.getInstancesFromEureka(serverId);
        return result.getSuccess(JSON.toJSONString(list));
    }


}

package jrx.anydmp.gateway.admin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jrx.anydmp.gateway.admin.service.IServerInfoService;
import jrx.anydmp.gateway.dto.ServerInfoDto;
import jrx.anytxn.common.data.TxnPage;
import jrx.anytxn.common.data.TxnRespResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;

/**
 * 服务信息 api
 *
 * @author zhao tingting
 * @date 2018/10/25
 */
@Api(description = "服务信息")
@RestController
@RequestMapping(value = "/serverInfo")
public class ServerInfoController {

    @Autowired
    private IServerInfoService serverInfoService;

    /**
     * 添加服务信息
     * @param serverInfoDto 服务信息对象
     * @return
     */
    @PostMapping
    @ApiOperation(value = "添加服务信息")
    TxnRespResult<ServerInfoDto> create(@Valid @RequestBody ServerInfoDto serverInfoDto) {
        TxnRespResult result = new TxnRespResult();
        ServerInfoDto dto = serverInfoService.add(serverInfoDto);
        return result.getSuccess(dto);
    }


    /**
     * 删除服务信息
     * @param  serverId 主键id
     * @return
     *
     */
    @ApiOperation(value = "删除服务信息")
    @PutMapping("/serverId/{serverId}")
    TxnRespResult<Boolean> delete(@PathVariable String serverId) {
        TxnRespResult result = new TxnRespResult();
        Boolean flag = serverInfoService.remove(serverId);
        return result.getSuccess(flag);
    }

    /**
     * 编辑服务信息
     * @param serverInfoDto 服务信息对象
     * @return
     *
     */
    @ApiOperation(value = "编辑服务信息")
    @PutMapping
    TxnRespResult<ServerInfoDto> update(@Valid @RequestBody ServerInfoDto serverInfoDto) {
        TxnRespResult result = new TxnRespResult();
        ServerInfoDto infoDto = serverInfoService.modify(serverInfoDto);
        return result.getSuccess(infoDto);
    }

    /**
     * 根据服务ID修改服务状态
     * @param serverId 服务ID
     * @param status 状态
     * @return
     *
     */
    @ApiOperation(value = "根据服务ID修改服务状态")
    @PutMapping("/serverId/{serverId}/status/{status}")
    TxnRespResult<Boolean> updateByStatus(@PathVariable String serverId,@PathVariable Integer status) {
        TxnRespResult result = new TxnRespResult();
        Boolean flag = serverInfoService.modifyByStauts(serverId, status);
        return result.getSuccess(flag);
    }

    /**
     * 根据id获取服务信息
     * @param serverId 主键id
     * @return ServerInfoDto
     *
     */
    @ApiOperation(value = "根据id获取服务信息")
    @GetMapping("/serverId/{serverId}")
    TxnRespResult<ServerInfoDto> getById(@PathVariable String serverId) {
        TxnRespResult result = new TxnRespResult();
        ServerInfoDto infoDto = serverInfoService.getByServerId(serverId);
        return result.getSuccess(infoDto);
    }

    /**
     * 分页查询服务信息
     * @param pageNum 每页条数
     * @param pageSize 页码
     * @return ServerInfoDto
     *
     */
    @ApiOperation(value = "分页查询网关服务信息")
    @GetMapping("/page")
    TxnRespResult<TxnPage<ServerInfoDto>> getAllPage(@RequestParam(defaultValue = "1", required = false) Integer pageNum,
                                                     @RequestParam(defaultValue = "10", required = false) Integer pageSize,
													 @RequestParam(required = false)String serverId) {
        TxnRespResult result = new TxnRespResult();
		TxnPage<ServerInfoDto> page = serverInfoService.getAllPage(pageNum, pageSize, serverId);
        return result.getSuccess(page);
    }


    /**
	 * 获取非删除状态的服务ID和名称，用于选择的下来菜单
	 * @return
	 */
	@ApiOperation(value = "获取非删除状态的服务ID和名称")
	@GetMapping("/names")
	TxnRespResult<ArrayList<ServerInfoDto>> getIdAndNameList(){
		TxnRespResult<ArrayList<ServerInfoDto>> result = new TxnRespResult();
		ArrayList<ServerInfoDto> list = (ArrayList<ServerInfoDto>) serverInfoService.getIdAndNameList();
		return result.getSuccess(list);
	}


	/**
	 * 从注册中心上获取所有服务
	 * @return
	 */
	@ApiOperation(value = "从注册中心上获取所有服务")
	@GetMapping("/list")
	TxnRespResult<ArrayList<ServerInfoDto>> getAllServerFromEureka(){
		TxnRespResult<ArrayList<ServerInfoDto>> result = new TxnRespResult();
		ArrayList<ServerInfoDto> list = (ArrayList<ServerInfoDto>) serverInfoService.getAllServerFromEureka();
		return result.getSuccess(list);
	}

	/**
	 * 是否可以删除网关服务
	 *
	 * @param serverId 网关服务id
	 * @return Boolean
	 */
	@ApiOperation(value = "是否可以删除网关服务")
	@GetMapping("/isDelete/serverId/{serverId}")
	TxnRespResult<Boolean> isDelete(@PathVariable String serverId) {
		TxnRespResult result = new TxnRespResult();
		Boolean flag = serverInfoService.isDelete(serverId);
		return result.getSuccess(flag);
	}
}

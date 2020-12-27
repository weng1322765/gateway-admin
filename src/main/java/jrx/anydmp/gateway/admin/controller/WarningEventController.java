package jrx.anydmp.gateway.admin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jrx.anydmp.gateway.admin.service.IWarningEventService;
import jrx.anydmp.gateway.dto.WarningEventDto;
import jrx.anytxn.common.data.TxnPage;
import jrx.anytxn.common.data.TxnRespResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhao tingting
 * @date 2018/12/6
 */
@Api(description = "限流预警")
@RestController
@RequestMapping(value = "/warningEvent")
public class WarningEventController {

	@Autowired
	private IWarningEventService warningEventService;
	/**
	 * 分页获取限流预警信息
	 * @param serverId 服务id
	 * @param instanceId 实例id
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@ApiOperation(value = "分页查询限流预警信息")
	@GetMapping("/page")
	TxnRespResult<TxnPage<WarningEventDto>> getAllPage(@RequestParam(required = false) String serverId,
													   @RequestParam(required = false) String instanceId,
													   @RequestParam(defaultValue = "1", required = false) Integer pageNum,
													   @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		TxnRespResult result = new TxnRespResult();
		TxnPage<WarningEventDto> page = warningEventService.getPageByServerIdAndInstanceId(serverId, instanceId, pageNum, pageSize);
		return result.getSuccess(page);
	}


	/**
	 * 根据id获取限流预警信息
	 * @param id 主键id
	 * @return WarningEventDto
	 *
	 */
	@ApiOperation(value = "根据id获取限流预警信息")
	@GetMapping("/id/{id}")
	TxnRespResult<WarningEventDto> getById(@PathVariable Integer id) {
		TxnRespResult result = new TxnRespResult();
		WarningEventDto infoDto = warningEventService.getById(id);
		return result.getSuccess(infoDto);
	}
}

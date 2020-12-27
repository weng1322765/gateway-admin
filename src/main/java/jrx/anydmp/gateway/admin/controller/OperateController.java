package jrx.anydmp.gateway.admin.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jrx.anydmp.gateway.admin.service.IOperateService;
import jrx.anydmp.gateway.dto.OperateEventDto;
import jrx.anytxn.common.data.TxnPage;
import jrx.anytxn.common.data.TxnRespResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 操作记录 api
 * @author zhao tingting
 * @date 2018/11/15
 */
@Api(description = "操作记录")
@RestController
@RequestMapping(value = "/recordInfo")
public class OperateController {

	@Autowired
	private IOperateService operateService;

	/**
	 * 分页查询操作记录信息
	 * @param user 更信人
	 * @param type 操作类型
	 * @param pageNum 每页条数
	 * @param pageSize 页码
	 * @return DegradeDto
	 *
	 */
	@ApiOperation(value = "分页查询操作记录信息")
	@GetMapping("/page")
	TxnRespResult<TxnPage<OperateEventDto>> getAllPage(@RequestParam(required = false) String user,
													   @RequestParam(required = false) String type,
													   @RequestParam(defaultValue = "1", required = false) Integer pageNum,
													   @RequestParam(defaultValue = "10", required = false) Integer pageSize) {
		TxnRespResult result = new TxnRespResult();
		TxnPage<OperateEventDto> page = operateService.getPageByUserAndType(user, type, pageNum, pageSize);
		return result.getSuccess(page);
	}


	/**
	 * 根据id获取操作记录信息
	 * @param id 主键id
	 * @return OperateEventDto
	 *
	 */
	@ApiOperation(value = "根据id获取操作记录信息")
	@GetMapping("/id/{id}")
	TxnRespResult<OperateEventDto> getById(@PathVariable Integer id) {
		TxnRespResult result = new TxnRespResult();
		OperateEventDto infoDto = operateService.getById(id);
		return result.getSuccess(infoDto);
	}
}

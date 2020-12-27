package jrx.anydmp.gateway.admin.service;

import jrx.anydmp.gateway.dto.WarningEventDto;
import jrx.anytxn.common.data.TxnPage;

/**
 * 限流预警信息 业务接口
 * @author zhao tingting
 * @date 2018/12/6
 */
public interface IWarningEventService {

	/**
	 * 分页获取限流预警信息
	 * @param serverId 服务id
	 * @param instanceId 实例id
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	TxnPage<WarningEventDto> getPageByServerIdAndInstanceId(String serverId, String instanceId, Integer pageNum, Integer pageSize);


	/**
	 * 根据id获取限流预警信息
	 * @param id 主键id
	 * @return WarningEventDto
	 *
	 */
	WarningEventDto getById(Integer id);

}

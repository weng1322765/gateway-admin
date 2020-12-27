package jrx.anydmp.gateway.admin.service;

import jrx.anydmp.gateway.admin.dto.CommonProperty;
import jrx.anydmp.gateway.admin.dto.OperateResult;

/**
 * @author zhao tingting
 * @date 2019/1/3
 */
public interface ICommonService {

	/**
	 * 根据serverId查询该服务下是否存在已删除的服务
	 *	@param serverId 服务id
	 *
	 */
	Boolean ifServerExsit(String serverId);

	/**
	 * 1.将对路由，限流，降级的操作保存到operate_event表中
	 * 2.从operate_event表里读数据向网关系统发送请求
	 * @param object 操作对象
	 * @param operateType 操作类型
	 *
	 */
	OperateResult handleGateway(CommonProperty object, String operateType);

}

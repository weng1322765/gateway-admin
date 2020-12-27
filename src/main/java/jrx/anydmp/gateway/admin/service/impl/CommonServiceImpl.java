package jrx.anydmp.gateway.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jrx.anydmp.gateway.admin.dto.CommonProperty;
import jrx.anydmp.gateway.admin.dto.OperateResult;
import jrx.anydmp.gateway.admin.service.ICommonService;
import jrx.anydmp.gateway.admin.service.IOperateService;
import jrx.anydmp.gateway.entity.OperateEvent;
import jrx.anydmp.gateway.mapper.ServerInfoMapper;
import jrx.anytxn.common.exception.TxnArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author zhao tingting
 * @date 2019/1/3
 */
@Service
public class CommonServiceImpl implements ICommonService{

	private static final Logger log = LoggerFactory.getLogger(CommonServiceImpl.class);

	@Autowired
	private ServerInfoMapper serverInfoMapper;

	@Autowired
	private IOperateService operateService;

	/**
	 * 根据serverId查询该服务下是否存在已删除的服务
	 *
	 * @param serverId 服务id
	 */
	@Override
	public Boolean ifServerExsit(String serverId) {
		QueryWrapper qw = new QueryWrapper();
		if (!StringUtils.isEmpty(serverId)) {
			qw.eq("server_id", serverId);
			qw.eq("status", -1);
		}
		return serverInfoMapper.selectCount(qw) > 0;
	}

	/**
	 * 1.将对路由，限流，降级的操作保存到operate_event表中
	 * 2.从operate_event表里读数据向网关系统发送请求
	 *
	 * @param dto      操作对象
	 * @param operateType 操作类型
	 */
	@Override
	public OperateResult handleGateway(CommonProperty dto, String operateType) {
		OperateResult result = operateService.formatOperateEvent(new OperateEvent()
				.setServerId(dto.getServerId())
				.setType(dto.getType())
				.setOperateType(operateType)
				.setObjectId(dto.getId())
		);
		if (result == null) {
			log.error("该服务" + dto.getServerId() + "没有可用实例") ;
			throw new TxnArgumentException("该服务没有可用实例");
		}
		result = operateService.informGateway(result.getOperateEventList(), result);
		return result;
	}
}

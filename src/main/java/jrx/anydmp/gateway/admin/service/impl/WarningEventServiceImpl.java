package jrx.anydmp.gateway.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jrx.anydmp.gateway.admin.service.IWarningEventService;
import jrx.anydmp.gateway.dto.WarningEventDto;
import jrx.anydmp.gateway.entity.ServerInfo;
import jrx.anydmp.gateway.entity.WarningEvent;
import jrx.anydmp.gateway.mapper.ServerInfoMapper;
import jrx.anydmp.gateway.mapper.WarningEventMapper;
import jrx.anytxn.common.data.TxnPage;
import jrx.anytxn.common.exception.TxnArgumentException;
import jrx.anytxn.common.utils.BeanMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 限流预警信息 业务接口实现类
 * @author zhao tingting
 * @date 2018/12/6
 */
@Service
public class WarningEventServiceImpl implements IWarningEventService{

	@Autowired
	private WarningEventMapper warningEventMapper;

	@Autowired
	private ServerInfoMapper serverInfoMapper;

	/**
	 * 分页获取限流预警信息
	 *
	 * @param serverId   服务id
	 * @param instanceId 实例id
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@Override
	public TxnPage<WarningEventDto> getPageByServerIdAndInstanceId(String serverId, String instanceId, Integer pageNum, Integer pageSize) {
		Page<WarningEvent> page = new Page(pageNum, pageSize);
		QueryWrapper wrapper = new QueryWrapper();
		if (!StringUtils.isEmpty(serverId)) {
			wrapper.eq("server_id", serverId);
		}

		if (!StringUtils.isEmpty(instanceId)) {
			wrapper.eq("instance_name", instanceId);
		}
		wrapper.orderByDesc("id");
		warningEventMapper.selectPage(page, wrapper);
		List<WarningEventDto> dtoList = BeanMapping.copyList(page.getRecords(), WarningEventDto.class);

		for (WarningEventDto warningEventDto : dtoList) {
			ServerInfo serverInfo = serverInfoMapper.selectById(warningEventDto.getServerId());
			if (serverInfo != null) {
				warningEventDto.setServerName(serverInfo.getServerName());
			}
		}
		return new TxnPage(pageNum, pageSize, page.getTotal(), dtoList);
	}

	/**
	 * 根据id获取限流预警信息
	 *
	 * @param id 主键id
	 * @return WarningEventDto
	 */
	@Override
	public WarningEventDto getById(Integer id) {
		if (StringUtils.isEmpty(id)) {
			throw new TxnArgumentException("Id不可为空");
		}
		WarningEvent warningEvent = warningEventMapper.selectById(id);
		if (warningEvent == null) {
			throw new TxnArgumentException("该id下的限流预警信息不存在");
		}
		ServerInfo serverInfo = new ServerInfo();
		if (!StringUtils.isEmpty(warningEvent.getServerId())) {
			 serverInfo = serverInfoMapper.selectById(warningEvent.getServerId());
		}
		WarningEventDto warningEventDto = BeanMapping.copy(warningEvent, WarningEventDto.class);
		warningEventDto.setServerName(serverInfo.getServerName());
		return warningEventDto;
	}
}

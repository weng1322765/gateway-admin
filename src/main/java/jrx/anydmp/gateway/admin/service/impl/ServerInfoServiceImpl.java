package jrx.anydmp.gateway.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import jrx.anydmp.gateway.admin.constant.Constant;
import jrx.anydmp.gateway.admin.enums.Status;
import jrx.anydmp.gateway.admin.service.IServerInfoService;
import jrx.anydmp.gateway.dto.ServerInfoDto;
import jrx.anydmp.gateway.entity.ServerInfo;
import jrx.anydmp.gateway.mapper.*;
import jrx.anytxn.common.data.TxnPage;
import jrx.anytxn.common.exception.TxnArgumentException;
import jrx.anytxn.common.utils.BeanMapping;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务信息 业务接口实现类
 * @author zhao tingting
 * @date 2018/10/25
 */
@Service
public class ServerInfoServiceImpl implements IServerInfoService{

    @Autowired
    private ServerInfoMapper serverInfoMapper;

    @Autowired
    private EurekaClient eurekaClient;

    @Autowired
	private RouteInfoMapper routeInfoMapper;

    @Autowired
	private DegradeMapper degradeMapper;

    @Autowired
	private FlowLimitMapper flowLimitMapper;

    @Autowired
	private IServerInfoService serverInfoService;

    /**
     * 添加服务信息
     *
     * @param serverInfoDto 服务信息对象
     * @return ServerInfoDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServerInfoDto add(ServerInfoDto serverInfoDto) {
        ServerInfo info = serverInfoMapper.selectById(serverInfoDto.getServerId());
        ServerInfo serverInfo = BeanMapping.copy(serverInfoDto, ServerInfo.class);
        serverInfo.setCreateTime(LocalDateTime.now());
        serverInfo.setUpdateTime(LocalDateTime.now());
        serverInfo.setStatus(Status.DISABLE.getCode());
        serverInfo.setUpdateBy(Constant.DEFAULT_USER);
		if (info != null) {
			if (info.getStatus() == -1) {
				serverInfoMapper.updateByPrimaryKey(serverInfo);
			} else {
				throw new TxnArgumentException("已存在的服务处于非删除状态下不可创建");
			}

		} else {
			serverInfoMapper.insert(serverInfo);
		}
        return BeanMapping.copy(serverInfo,ServerInfoDto.class);
    }

    /**
     * 删除服务信息
     *
     * @param serverId 主键id
     * @return Boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean remove(String serverId) {
        ServerInfo info = serverInfoMapper.selectById(serverId);
        if (info == null || info.getStatus().equals(Status.DELETE.getCode())) {
            throw new TxnArgumentException("该服务已不存在");
        }
        // 服务状态为启用状态下不可删除
        if (info.getStatus().equals(Status.ENABLE.getCode())) {
            throw new TxnArgumentException("状态为启用时不可删除");
        }

		if (!serverInfoService.isDelete(serverId)) {
			throw new TxnArgumentException("该服务还在使用中无法删除");
		}
        // 将该记录状态改为-1
		info.setStatus(Status.DELETE.getCode());
        return serverInfoMapper.updateById(info) == 1;
    }

    /**
     * 编辑服务信息
     *
     * @param serverInfoDto 服务信息对象
     * @return ServerInfoDto
     */
    @Override
    public ServerInfoDto modify(ServerInfoDto serverInfoDto) {
        ServerInfo serverInfo = serverInfoMapper.selectById(serverInfoDto.getServerId());
        if (serverInfo == null) {
            throw new TxnArgumentException("需要更新的记录不存在");
        }
        ServerInfo info = BeanMapping.copy(serverInfoDto, ServerInfo.class);
        info.setUpdateTime(LocalDateTime.now());
        serverInfoMapper.updateById(info);
        return BeanMapping.copy(info,ServerInfoDto.class);
    }

    /**
     * 根据服务ID编辑服务状态
     *
     * @param serverId 服务ID
     * @param status   服务状态
     * @return Boolean
     */
    @Override
    public Boolean modifyByStauts(String serverId, Integer status) {
        if (StringUtils.isEmpty(serverId)) {
            throw new TxnArgumentException("服务Id不可为空");
        }
        ServerInfo serverInfo = serverInfoMapper.selectById(serverId);
        if (serverInfo == null || serverInfo.getStatus().equals(Status.DELETE.getCode()) ) {
            throw new TxnArgumentException("该服务不存在");
        }
        ServerInfo serverInfoReq = new ServerInfo();
        serverInfoReq.setServerId(serverId);
        serverInfoReq.setStatus(status);
        int flag = serverInfoMapper.updateById(serverInfoReq);
        return flag == 1;
    }

    /**
     * 根据id获取服务信息
     *
     * @param serverId 主键id
     * @return ServerInfoDto
     */
    @Override
    public ServerInfoDto getByServerId(String serverId) {
        if (StringUtils.isEmpty(serverId)) {
            throw new TxnArgumentException("Id不可为空");
        }
        ServerInfo serverInfo = serverInfoMapper.selectById(serverId);

		if (serverInfo == null || serverInfo.getStatus() == -1) {
			throw new TxnArgumentException("该服务已不存在");
		}
        ServerInfoDto serverInfoDto = new ServerInfoDto();
        BeanUtils.copyProperties(serverInfo,serverInfoDto);

        // 查询所有开启状态和关闭状态的路由
		int routeEnable = routeInfoMapper.countByServerIdAndStatus(serverId, Status.ENABLE.getCode());
		int routeDisable = routeInfoMapper.countByServerIdAndStatus(serverId, Status.DISABLE.getCode());

		// 查询所有开启状态和关闭状态的限流
		int flowEnableLimit = flowLimitMapper.countByServerIdAndStatus(serverId, Status.ENABLE.getCode());
		int flowDisableLimit = flowLimitMapper.countByServerIdAndStatus(serverId, Status.DISABLE.getCode());

		// 查询所有开启状态和关闭状态的降级
		int degradeEnable = degradeMapper.countByServerIdAndStatus(serverId, Status.ENABLE.getCode());
		int degradeDisable = degradeMapper.countByServerIdAndStatus(serverId, Status.DISABLE.getCode());

		serverInfoDto.setRouteUpTotal(routeEnable);
		serverInfoDto.setRouteDownTotal(routeDisable);

		serverInfoDto.setLimitUpTotal(flowEnableLimit);
		serverInfoDto.setLimitDownTotal(flowDisableLimit);

		serverInfoDto.setDegradeUpTotal(degradeEnable);
		serverInfoDto.setDegradeDownTotal(degradeDisable);
        return serverInfoDto;
    }

    /**
     * 分页查询服务信息
     *
     * @param pageNum  每页条数
     * @param pageSize 页码
     * @return ServerInfoDto
     */
    @Override
    public TxnPage<ServerInfoDto> getAllPage(Integer pageNum, Integer pageSize,String serverId) {
        Page<ServerInfo> page = new Page(pageNum, pageSize);
        QueryWrapper wrapper = new QueryWrapper();
        if(!StringUtils.isEmpty(serverId)) {
            wrapper.like("SERVER_NAME", serverId);
        }
        wrapper.ne("STATUS",-1);
        serverInfoMapper.selectPage(page, wrapper);
        List<ServerInfoDto> dtoList = BeanMapping.copyList(page.getRecords(), ServerInfoDto.class);
        return new TxnPage(pageNum, pageSize, page.getTotal(), dtoList);
    }


    /**
     * 获取非删除状态的服务ID和名称，用于选择的下拉菜单
     *
     * @return
     */
    @Override
    public List<ServerInfoDto> getIdAndNameList() {

		List<ServerInfo> list = serverInfoMapper.getIdAndNameList();

		return BeanMapping.copyList(list, ServerInfoDto.class);
    }

	/**
	 * 从注册中心上获取所有服务
	 *
	 * @return
	 */
	@Override
	public List<ServerInfoDto> getAllServerFromEureka() {
		Applications applications = eurekaClient.getApplications();
		List<Application> appList = applications.getRegisteredApplications();

		List<ServerInfoDto> serverInfoDtoList = new ArrayList<>();
		for (int i=0;i<appList.size();i++) {
			ServerInfoDto serverInfoDto = new ServerInfoDto();
			serverInfoDto.setServerId(appList.get(i).getName());
			serverInfoDtoList.add(serverInfoDto);
		}
		return serverInfoDtoList;
	}

	/**
	 * 是否可以删除网关服务
	 *
	 * @param serverId 网关服务id
	 * @return Boolean
	 */
	@Override
	public Boolean isDelete(String serverId) {
		int routeCnt = routeInfoMapper.countByServerIdAndStatus(serverId, Status.DELETE.getCode());
		int flowCnt = flowLimitMapper.countByServerIdAndStatus(serverId, Status.DELETE.getCode());
		int degradeCnt = degradeMapper.countByServerIdAndStatus(serverId, Status.DELETE.getCode());
		if (routeCnt > 0 || flowCnt > 0 || degradeCnt > 0) {
			return false;
		} else {
			return true;
		}
	}

}

package jrx.anydmp.gateway.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jrx.anydmp.gateway.admin.constant.Constant;
import jrx.anydmp.gateway.admin.enums.Status;
import jrx.anydmp.gateway.admin.service.IRouteInfoService;
import jrx.anydmp.gateway.dto.RouteInfoDto;
import jrx.anydmp.gateway.entity.RouteInfo;
import jrx.anydmp.gateway.entity.ServerInfo;
import jrx.anydmp.gateway.mapper.DegradeMapper;
import jrx.anydmp.gateway.mapper.FlowLimitMapper;
import jrx.anydmp.gateway.mapper.RouteInfoMapper;
import jrx.anydmp.gateway.mapper.ServerInfoMapper;
import jrx.anytxn.common.data.TxnPage;
import jrx.anytxn.common.exception.TxnArgumentException;
import jrx.anytxn.common.utils.BeanMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 路由信息 业务接口实现
 * @author zhao tingting
 * @date 2018/10/25
 */
@Service
public class RouteInfoServiceImpl implements IRouteInfoService{

    @Autowired
    private RouteInfoMapper routeInfoMapper;

    @Autowired
	private ServerInfoMapper serverInfoMapper;

    @Autowired
	private FlowLimitMapper flowLimitMapper;

    @Autowired
	private DegradeMapper degradeMapper;

	@Autowired
	private CommonServiceImpl commonService;

    /**
     * 添加路由信息
     *
     * @param routeInfoDto 路由信息对象
     * @return RouteInfoDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RouteInfoDto add(RouteInfoDto routeInfoDto) {
		if (routeInfoDto.getRouteUrl().equals(routeInfoDto.getInterfaceUrl())) {
			throw new TxnArgumentException("路由路径与接口路径不可相同");
		}
		if (commonService.ifServerExsit(routeInfoDto.getServerId())){
			throw new TxnArgumentException("选择的服务已经删除");
		}
        RouteInfo routeInfo = BeanMapping.copy(routeInfoDto, RouteInfo.class);
        routeInfo.setCreateTime(LocalDateTime.now());
        routeInfo.setUpdateTime(LocalDateTime.now());
        routeInfo.setStatus(Status.DISABLE.getCode());
        routeInfo.setUpdateBy(Constant.DEFAULT_USER);
        routeInfoMapper.insert(routeInfo);
        return routeInfoDto;
    }

    /**
     * 删除路由信息
     *
     * @param id 主键id
     * @return Boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RouteInfoDto remove(Integer id) {
		RouteInfo info =routeInfoMapper.selectById(id);
		if (info == null) {
			throw new TxnArgumentException("需要删除的路由不存在");
		}
		// 路由状态为启用状态下不可删除
		if (info.getStatus().equals(Status.DELETE.getCode())) {
			throw new TxnArgumentException("该路由已删除");
		}
	     // 路由状态为启用状态下不可删除
        if (info.getStatus().equals(Status.ENABLE.getCode())) {
            throw new TxnArgumentException("状态为启用时不可删除");
        }
		// 将该记录状态改为-1
		info.setStatus(Status.DELETE.getCode());
        routeInfoMapper.updateById(info);
        return BeanMapping.copy(info, RouteInfoDto.class);
    }

    /**
     * 编辑路由信息
     *
     * @param routeInfoDto 路由信息对象
     * @return RouteInfoDto
     */
    @Override
    public RouteInfoDto modify(RouteInfoDto routeInfoDto) {
        RouteInfo routeInfo = routeInfoMapper.selectById(routeInfoDto.getId());
        if (routeInfo == null) {
            throw new TxnArgumentException("需要更新的记录不存在");
        }
		if (commonService.ifServerExsit(routeInfoDto.getServerId())){
			throw new TxnArgumentException("选择的服务已经删除");
		}
        RouteInfo info = BeanMapping.copy(routeInfoDto, RouteInfo.class);
        info.setUpdateTime(LocalDateTime.now());
        routeInfoMapper.updateById(info);
        info.setStatus(routeInfo.getStatus());
        return BeanMapping.copy(info,RouteInfoDto.class);
    }

    /**
     * 根据serverId获取路由信息
     *
     * @param serverId 服务id
     * @return List<RouteInfoDto>
     */
    @Override
    public List<RouteInfoDto> getByServerId(String serverId) {
        List<RouteInfo> routeInfoList = routeInfoMapper.selectByServerId(serverId);
        return BeanMapping.copyList(routeInfoList,RouteInfoDto.class);
    }

    /**
     * 根据id获取路由信息
     *
     * @param id 主键id
     * @return RouteInfoDto
     */
    @Override
    public RouteInfoDto getById(Integer id) {
        if (StringUtils.isEmpty(id)) {
            throw new TxnArgumentException("Id不可为空");
        }
        RouteInfo routeInfo = routeInfoMapper.selectById(id);
		if (routeInfo == null || routeInfo.getStatus() == -1) {
			throw new TxnArgumentException("该路由信息记录不存在");
		}
        return BeanMapping.copy(routeInfo,RouteInfoDto.class);
    }

    /**
     * 分页获取路由信息
     *
     * @param serverId
     * @param routeName
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public TxnPage<RouteInfoDto> getPageByServerIdAndRouteName(String serverId, String routeName, Integer pageNum, Integer pageSize) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq(!StringUtils.isEmpty(serverId), "SERVER_ID", serverId);
        wrapper.like(!StringUtils.isEmpty(routeName),"ROUTE_NAME", routeName);
        //非删除
        wrapper.ne("STATUS", -1);
        wrapper.orderByDesc("STATUS");

        Page<RouteInfo> page = new Page(pageNum, pageSize);
        routeInfoMapper.selectPage(page, wrapper);
        List<RouteInfoDto> dtoList = BeanMapping.copyList(page.getRecords(), RouteInfoDto.class);

		for (RouteInfoDto routeInfoDto : dtoList) {
			ServerInfo serverInfo = serverInfoMapper.selectById(routeInfoDto.getServerId());
			if (serverInfo != null) {
				routeInfoDto.setServerName(serverInfo.getServerName());
			}
		}
        return new TxnPage(pageNum, pageSize, page.getTotal(), dtoList);
    }

	/**
	 * 根据ID编辑路由状态
	 *
	 * @param id     路由ID
	 * @param status 路由状态
	 * @return Boolean
	 */
    @Override
    public RouteInfoDto modifyByStauts(Integer id, Integer status) {
        if (StringUtils.isEmpty(id)) {
            throw new TxnArgumentException("路由Id不可为空");
        }
        RouteInfo routeInfo = routeInfoMapper.selectById(id);
		if (routeInfo == null || routeInfo.getStatus().equals(Status.DELETE.getCode())) {
			throw new TxnArgumentException("该路由不存在");
		}
        RouteInfo routeInfoReq = new RouteInfo();
        routeInfoReq.setId(id);
        routeInfoReq.setStatus(status);
        routeInfoMapper.updateById(routeInfoReq);
        return BeanMapping.copy(routeInfo, RouteInfoDto.class);
    }

	/**
	 * 获取非删除状态的路由ID和名称，用于选择的下来菜单
	 *
	 * @return
	 */
	@Override
//	@Scheduled(initialDelay = 10 * 1000L, fixedDelay = 10 * 1000L)
	public List<RouteInfoDto> getIdAndNameList(String serverId) {
		List<RouteInfo> list;
		if (StringUtils.isEmpty(serverId)) {
			list = routeInfoMapper.getIdAndNameList();
		} else {
			list =routeInfoMapper.getIdAndNameByServerId(serverId);
		}

		return BeanMapping.copyList(list, RouteInfoDto.class);
	}

	/**
	 * 获取非删除状态的路由ID和名称，用于选择的下来菜单
	 *
	 * @param serverId
	 * @param status
	 * @return
	 */
	@Override
	public List<RouteInfoDto> getByServerIdAndStatus(String serverId, Integer status) {
		if (StringUtils.isEmpty(serverId)) {
			throw new TxnArgumentException("serverId不可为空");
		}
		if (status == null) {
			throw new TxnArgumentException("状态不可为空");
		}
		List<RouteInfo> routeInfoList = routeInfoMapper.selectByServerIdAndStatus(serverId, status);
		return BeanMapping.copyList(routeInfoList,RouteInfoDto.class);
	}

	/**
	 * 是否可以删除路由
	 *
	 * @param id 主键id
	 * @return Boolean
	 */
	@Override
	public Boolean isDelete(Integer id) {
		int flowCnt = flowLimitMapper.selectByRouteId(id);
		int degradeCnt = degradeMapper.selectByRouteId(id);
		if (flowCnt > 0 || degradeCnt > 0) {
			return false;
		} else {
			return true;
		}

	}
}

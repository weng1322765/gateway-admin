package jrx.anydmp.gateway.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jrx.anydmp.gateway.admin.constant.Constant;
import jrx.anydmp.gateway.admin.enums.Status;
import jrx.anydmp.gateway.admin.service.IFlowLimitService;
import jrx.anydmp.gateway.dto.FlowLimitDto;
import jrx.anydmp.gateway.entity.FlowLimit;
import jrx.anydmp.gateway.entity.RouteInfo;
import jrx.anydmp.gateway.entity.ServerInfo;
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
 * 限流控制 业务接口实现类
 * @author zhao tingting
 * @date 2018/10/25
 */
@Service
public class FlowLimitServiceImpl implements IFlowLimitService {

    @Autowired
    private FlowLimitMapper flowLimitMapper;

    @Autowired
	private RouteInfoMapper routeInfoMapper;

    @Autowired
    private ServerInfoMapper serverInfoMapper;

    @Autowired
    private CommonServiceImpl commonService;

    /**
     * 添加限流信息
     *
     * @param flowLimitDto 限流信息对象
     * @return FlowLimitDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlowLimitDto add(FlowLimitDto flowLimitDto) {
        if (StringUtils.isEmpty(flowLimitDto.getRouteId()) && StringUtils.isEmpty(flowLimitDto.getUrl())) {
            throw new TxnArgumentException("路由名称和限流地址不可都为空");
        }
        if (commonService.ifServerExsit(flowLimitDto.getServerId())){
            throw new TxnArgumentException("选择的服务已经删除");
        }
        FlowLimit flowLimit = BeanMapping.copy(flowLimitDto, FlowLimit.class);
        flowLimit.setCreateTime(LocalDateTime.now());
        flowLimit.setUpdateTime(LocalDateTime.now());
        flowLimit.setStatus(Status.DISABLE.getCode());
        flowLimit.setUpdateBy(Constant.DEFAULT_USER);
        if (ifExists(flowLimit)) {
            throw new TxnArgumentException("同一路由或URL限流规则只能同时存在一条记录");
        }
        flowLimitMapper.insert(flowLimit);
        return flowLimitDto;
    }

    private boolean ifExists(FlowLimit flowLimit) {
        QueryWrapper qw = new QueryWrapper();
        if (StringUtils.isEmpty(flowLimit.getRouteId())) {
            qw.eq("url", flowLimit.getUrl());
        } else {
            qw.eq("route_id", flowLimit.getRouteId());
        }
        qw.eq("server_id", flowLimit.getServerId());
        qw.ne("status", -1);
        return flowLimitMapper.selectCount(qw) > 0;
    }


    /**
     * 删除限流信息
     *
     * @param id 主键id
     * @return Boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlowLimitDto remove(Integer id) {
		FlowLimit info =flowLimitMapper.selectById(id);
		if (info == null) {
			throw new TxnArgumentException("需要删除的限流不存在");
		}
        // 限流状态删除状态
        if (info.getStatus().equals(Status.DELETE.getCode())) {
            throw new TxnArgumentException("该限流已删除");
        }
        // 限流状态为启用状态下不可删除
        if (info.getStatus().equals(Status.ENABLE.getCode())) {
            throw new TxnArgumentException("状态为启用时不可删除");
        }
		// 将该记录状态改为-1
        info.setStatus(Status.DELETE.getCode());
        flowLimitMapper.updateById(info);
        return BeanMapping.copy(info, FlowLimitDto.class);
    }

    /**
     * 编辑限流信息
     *
     * @param flowLimitDto 限流信息对象
     * @return FlowLimitDto
     */
    @Override
    public FlowLimitDto modify(FlowLimitDto flowLimitDto) {
        FlowLimit flowLimit = flowLimitMapper.selectById(flowLimitDto.getId());
        if (flowLimit == null) {
            throw new TxnArgumentException("需要更新的记录不存在");
        }
        if (commonService.ifServerExsit(flowLimitDto.getServerId())){
            throw new TxnArgumentException("选择的服务已经删除");
        }
        if (!flowLimitDto.getServerId().equals(flowLimit.getServerId()) ) {
            throw new TxnArgumentException("服务不可修改");
        }
        if (!StringUtils.isEmpty(flowLimitDto.getUrl()) && !flowLimit.getUrl().equals(flowLimitDto.getUrl())) {
            throw new TxnArgumentException("url不可修改");
        }
        FlowLimit limit = BeanMapping.copy(flowLimitDto, FlowLimit.class);
        limit.setUpdateTime(LocalDateTime.now());
        flowLimitMapper.updateById(limit);
        limit.setStatus(flowLimit.getStatus());
        return BeanMapping.copy(limit, FlowLimitDto.class);
    }

    /**
     * 根据serverId获取限流信息
     *
     * @param serverId 服务id
     * @return List<FlowLimitDto>
     */
    @Override
    public List<FlowLimitDto> getByServerId(String serverId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("server_id", serverId);
        qw.eq("status", Status.ENABLE.getCode());
        List<FlowLimit> list = flowLimitMapper.selectList(qw);
        return BeanMapping.copyList(list, FlowLimitDto.class);
    }

    /**
     * 根据id获取限流信息
     *
     * @param id 主键id
     * @return FlowLimitDto
     */
    @Override
    public FlowLimitDto getById(Integer id) {
        if (StringUtils.isEmpty(id)) {
            throw new TxnArgumentException("Id不可为空");
        }
        FlowLimit flowLimit = flowLimitMapper.selectById(id);
        if (flowLimit == null || flowLimit.getStatus() == -1) {
            throw new TxnArgumentException("该限流已不存在");
        }

        return BeanMapping.copy(flowLimit,FlowLimitDto.class);
    }

    /**
     * 分页查询限流信息
     *
     * @param pageNum  每页条数
     * @param pageSize 页码
     * @return FlowLimitDto
     */
    @Override
    public TxnPage<FlowLimitDto> getAllPage(Integer pageNum, Integer pageSize) {
        Page<FlowLimit> page = new Page(pageNum, pageSize);
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.ne("STATUS",-1);

        flowLimitMapper.selectPage(page, wrapper);
        List<FlowLimitDto> dtoList = BeanMapping.copyList(page.getRecords(), FlowLimitDto.class);
        return new TxnPage(pageNum, pageSize, page.getTotal(), dtoList);
    }

    /**
     * 根据网关和状态分页查询限流信息
     *
     * @param pageNum  每页条数
     * @param pageSize 页码
     * @param serverId 服务Id
     * @param status   状态
     * @return FlowLimitDto
     */
    @Override
    public TxnPage<FlowLimitDto> getPageByServerIdAndStatus(Integer pageNum, Integer pageSize, String serverId, Integer status) {
        Page<FlowLimit> page = new Page(pageNum, pageSize);
        QueryWrapper wrapper = new QueryWrapper();
        if (!StringUtils.isEmpty(serverId)) {
            wrapper.eq("server_id", serverId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
		wrapper.ne("STATUS",-1);
        flowLimitMapper.selectPage(page, wrapper);
        List<FlowLimitDto> dtoList = BeanMapping.copyList(page.getRecords(), FlowLimitDto.class);

		for (FlowLimitDto flowLimitDto : dtoList) {
			RouteInfo routeInfo = routeInfoMapper.selectById(flowLimitDto.getRouteId());
            ServerInfo serverInfo = serverInfoMapper.selectById(flowLimitDto.getServerId());
			if (routeInfo != null) {
				flowLimitDto.setRouteName(routeInfo.getRouteName());
			}
            if (serverInfo != null) {
                flowLimitDto.setServerName(serverInfo.getServerName());
            }
		}
        return new TxnPage(pageNum, pageSize, page.getTotal(), dtoList);
    }

	/**
	 * 根据ID编辑限流状态
	 *
	 * @param id     限流ID
	 * @param status 限流状态
	 * @return Boolean
	 */
    @Override
    public FlowLimitDto modifyByStauts(Integer id, Integer status) {
        if (StringUtils.isEmpty(id)) {
            throw new TxnArgumentException("限流Id不可为空");
        }
        FlowLimit flowLimit = flowLimitMapper.selectById(id);
        if (flowLimit == null || flowLimit.getStatus().equals(Status.DELETE.getCode())) {
            throw new TxnArgumentException("该限流不存在");
        }
        FlowLimit flowLimitReq = new FlowLimit();
        flowLimitReq.setId(id);
        flowLimitReq.setStatus(status);
        flowLimitMapper.updateById(flowLimitReq);
        return BeanMapping.copy(flowLimit, FlowLimitDto.class);
    }
}

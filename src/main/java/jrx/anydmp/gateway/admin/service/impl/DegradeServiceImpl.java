package jrx.anydmp.gateway.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jrx.anydmp.gateway.admin.constant.Constant;
import jrx.anydmp.gateway.admin.enums.Status;
import jrx.anydmp.gateway.admin.service.IDegradeService;
import jrx.anydmp.gateway.dto.DegradeDto;
import jrx.anydmp.gateway.entity.Degrade;
import jrx.anydmp.gateway.entity.FlowLimit;
import jrx.anydmp.gateway.entity.RouteInfo;
import jrx.anydmp.gateway.entity.ServerInfo;
import jrx.anydmp.gateway.mapper.DegradeMapper;
import jrx.anydmp.gateway.mapper.RouteInfoMapper;
import jrx.anydmp.gateway.mapper.ServerInfoMapper;
import jrx.anytxn.common.data.TxnPage;
import jrx.anytxn.common.exception.TxnArgumentException;
import jrx.anytxn.common.utils.BeanMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 降级控制 业务接口实现
 * @author zhao tingting
 * @date 2018/10/25
 */
@Service
@ComponentScan
public class DegradeServiceImpl implements IDegradeService {

    @Autowired
    private DegradeMapper degradeMapper;

	@Autowired
	private RouteInfoMapper routeInfoMapper;

	@Autowired
	private ServerInfoMapper serverInfoMapper;

	@Autowired
	private CommonServiceImpl commonService;
    /**
     * 添加降级信息
     *
     * @param degradeDto 降级信息对象
     * @return DegradeDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DegradeDto add(DegradeDto degradeDto) {
		if (StringUtils.isEmpty(degradeDto.getRouteId()) && StringUtils.isEmpty(degradeDto.getUrl())) {
			throw new TxnArgumentException("路由名称和降级地址不可都为空");
		}
		if (commonService.ifServerExsit(degradeDto.getServerId())){
			throw new TxnArgumentException("选择的服务已经删除");
		}
        Degrade degrade = BeanMapping.copy(degradeDto, Degrade.class);
        degrade.setCreateTime(LocalDateTime.now());
        degrade.setUpdateTime(LocalDateTime.now());
        degrade.setStatus(Status.DISABLE.getCode());
        degrade.setUpdateBy(Constant.DEFAULT_USER);
        if (ifExists(degrade)) {
            throw new TxnArgumentException("同一路由或URL降级规则只能同时存在一条记录");
        }
        degradeMapper.insert(degrade);
        return degradeDto;
    }

    private boolean ifExists(Degrade degrade) {
        QueryWrapper qw = new QueryWrapper();
        if (StringUtils.isEmpty(degrade.getRouteId())) {
            qw.eq("url", degrade.getUrl());
        } else {
            qw.eq("route_id", degrade.getRouteId());
        }
        qw.eq("server_id", degrade.getServerId());
        qw.ne("status", -1);
        return degradeMapper.selectCount(qw) > 0;
    }


    /**
     * 删除降级信息
     *
     * @param id 主键id
     * @return Boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DegradeDto remove(Integer id) {
		Degrade info = degradeMapper.selectById(id);
		if (info == null) {
			throw new TxnArgumentException("需要删除的降级不存在");
		}
		// 状态为删除状态
		if (info.getStatus().equals(Status.DELETE.getCode())) {
			throw new TxnArgumentException("该降级已删除");
		}
		// 状态为启用状态下不可删除
		if (info.getStatus().equals(Status.ENABLE.getCode())) {
            throw new TxnArgumentException("状态为启用时不可删除");
        }
		// 将该记录状态改为-1
		info.setStatus(Status.DELETE.getCode());
        degradeMapper.updateById(info);
        return BeanMapping.copy(info, DegradeDto.class);
    }

    /**
     * 编辑降级信息
     *
     * @param degradeDto 降级信息对象
     * @return DegradeDto
     */
    @Override
    public DegradeDto modify(DegradeDto degradeDto) {
        Degrade degrade = degradeMapper.selectById(degradeDto.getId());
        if (degrade == null) {
            throw new TxnArgumentException("需要更新的记录不存在");
        }
		if (commonService.ifServerExsit(degradeDto.getServerId())){
			throw new TxnArgumentException("选择服务已经删除");
		}
		if (!degradeDto.getServerId().equals(degrade.getServerId())) {
			throw new TxnArgumentException("服务不可修改");
		}
		if(!StringUtils.isEmpty(degradeDto.getUrl()) && !degradeDto.getUrl().equals(degrade.getUrl())){
			throw new TxnArgumentException("url不可修改");
		}

        Degrade degradeNew = BeanMapping.copy(degradeDto, Degrade.class);
        degradeNew.setUpdateTime(LocalDateTime.now());
        degradeMapper.updateById(degradeNew);
        degradeNew.setStatus(degrade.getStatus());
        return BeanMapping.copy(degradeNew, DegradeDto.class);
    }

    /**
     * 根据id获取降级信息
     *
     * @param id 主键id
     * @return DegradeDto
     */
    @Override
    public DegradeDto getById(Integer id) {
        if (StringUtils.isEmpty(id)) {
            throw new TxnArgumentException("Id不可为空");
        }
        Degrade degrade = degradeMapper.selectById(id);
		if (degrade == null || degrade.getStatus() == -1) {
			throw new TxnArgumentException("该降级已不存在");
		}
        return BeanMapping.copy(degrade,DegradeDto.class);
    }

    /**
     * 分页查询降级信息
     *
     * @param pageNum  每页条数
     * @param pageSize 页码
     * @return DegradeDto
     */
    @Override
    public TxnPage<DegradeDto> getAllPage(Integer pageNum, Integer pageSize) {
        Page<Degrade> page = new Page(pageNum, pageSize);
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.ne("STATUS",-1);

        degradeMapper.selectPage(page, wrapper);
        List<DegradeDto> dtoList = BeanMapping.copyList(page.getRecords(), DegradeDto.class);
        return new TxnPage(pageNum, pageSize, page.getTotal(), dtoList);
    }

    /**
     * 根据网关和状态分页查询降级控制信息
     *
     * @param pageNum  每页条数
     * @param pageSize 页码
     * @param serverId 服务Id
     * @param status   状态
     * @return DegradeDto
     */
    @Override
    public TxnPage<DegradeDto> getPageByServerIdAndStatus(Integer pageNum, Integer pageSize, String serverId, Integer status) {
        Page<Degrade> page = new Page(pageNum, pageSize);
        QueryWrapper wrapper = new QueryWrapper();
        if (!StringUtils.isEmpty(serverId)) {
            wrapper.eq("server_id", serverId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
		wrapper.ne("STATUS",-1);
        degradeMapper.selectPage(page, wrapper);
		List<DegradeDto> dtoList = BeanMapping.copyList(page.getRecords(), DegradeDto.class);

		for (DegradeDto degradeDto : dtoList) {
			RouteInfo routeInfo = routeInfoMapper.selectById(degradeDto.getRouteId());
			ServerInfo serverInfo = serverInfoMapper.selectById(degradeDto.getServerId());
			if (routeInfo != null) {
				degradeDto.setRouteName(routeInfo.getRouteName());
			}
			if (serverInfo != null) {
				degradeDto.setServerName(serverInfo.getServerName());
			}
		}
        return new TxnPage(pageNum, pageSize, page.getTotal(), dtoList);
    }

	/**
	 * 根据ID编辑降级状态
	 *
	 * @param id     降级ID
	 * @param status 降级状态
	 * @return Boolean
	 */
    @Override
    public DegradeDto modifyByStauts(Integer id, Integer status) {
        if (StringUtils.isEmpty(id)) {
            throw new TxnArgumentException("降级Id不可为空");
        }
        Degrade degrade = degradeMapper.selectById(id);
		if (degrade == null || degrade.getStatus().equals(Status.DELETE.getCode())) {
			throw new TxnArgumentException("该降级不存在");
		}
        Degrade degradeReq = new Degrade();
        degradeReq.setId(id);
        degradeReq.setStatus(status);
        degradeMapper.updateById(degradeReq);
        return BeanMapping.copy(degrade, DegradeDto.class);
    }

    @Override
    public List<DegradeDto> getByServerId(String serverId) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("server_id", serverId);
        qw.eq("status", Status.ENABLE.getCode());
        List<FlowLimit> list = degradeMapper.selectList(qw);
        return BeanMapping.copyList(list, DegradeDto.class);
    }
}

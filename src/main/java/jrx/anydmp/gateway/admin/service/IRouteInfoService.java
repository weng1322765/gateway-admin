package jrx.anydmp.gateway.admin.service;

import jrx.anydmp.gateway.dto.FlowLimitDto;
import jrx.anydmp.gateway.dto.RouteInfoDto;
import jrx.anydmp.gateway.entity.RouteInfo;
import jrx.anytxn.common.data.TxnPage;

import java.util.List;

/**
 * 路由信息 业务接口
 * @author zhao tingting
 * @date 2018/10/25
 */
public interface IRouteInfoService {

    /**
     * 添加路由信息
     * @param routeInfoDto 路由信息对象
     * @return RouteInfoDto
     *
     */
    RouteInfoDto add(RouteInfoDto routeInfoDto);

    /**
     * 删除路由信息
     * @param  id 主键id
     * @return Boolean
     *
     */
    RouteInfoDto remove(Integer id);


    /**
     * 编辑路由信息
     * @param routeInfoDto 路由信息对象
     * @return  RouteInfoDto
     *
     */
    RouteInfoDto modify(RouteInfoDto routeInfoDto);


    /**
     * 根据serverId获取路由信息
     * @param serverId 服务id
     * @return List<RouteInfoDto>
     *
     */
    List<RouteInfoDto> getByServerId(String serverId);


    /**
     * 根据id获取路由信息
     * @param id 主键id
     * @return RouteInfoDto
     *
     */
    RouteInfoDto getById(Integer id);


    /**
     * 分页获取路由信息
     * @param serverId
     * @param routeName
     * @param pageNum
     * @param pageSize
     * @return
     */
    TxnPage<RouteInfoDto> getPageByServerIdAndRouteName(String serverId, String routeName, Integer pageNum, Integer pageSize);


    /**
     * 根据ID编辑路由状态
     * @param id 路由ID
     * @param status 路由状态
     * @return  Boolean
     *
     */
    RouteInfoDto modifyByStauts(Integer id, Integer status);


    /**
     * 获取非删除状态的路由ID和名称，用于选择的下来菜单
     * @return
     */
    List<RouteInfoDto> getIdAndNameList(String serverId);

    /**
     * 获取非删除状态的路由ID和名称，用于选择的下来菜单
     * @return
     */
    List<RouteInfoDto> getByServerIdAndStatus(String serverId, Integer status);


    /**
     * 是否可以删除路由
     *
     * @param id 主键id
     * @return Boolean
     */
    Boolean isDelete(Integer id);

}

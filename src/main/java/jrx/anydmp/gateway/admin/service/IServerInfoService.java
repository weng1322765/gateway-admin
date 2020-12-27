package jrx.anydmp.gateway.admin.service;

import jrx.anydmp.gateway.dto.ServerInfoDto;
import jrx.anytxn.common.data.TxnPage;

import java.util.List;

/**
 * 服务信息 业务接口
 * @author zhao tingting
 * @date 2018/10/25
 */
public interface IServerInfoService {

    /**
     * 添加服务信息
     * @param serverInfoDto 服务信息对象
     * @return ServerInfoDto
     *
     */
    ServerInfoDto add(ServerInfoDto serverInfoDto);

    /**
     * 删除服务信息
     * @param  serverId 主键id
     * @return Boolean
     *
     */
    Boolean remove(String serverId);


    /**
     * 编辑服务信息
     * @param serverInfoDto 服务信息对象
     * @return  ServerInfoDto
     *
     */
    ServerInfoDto modify(ServerInfoDto serverInfoDto);


    /**
     * 根据服务ID编辑服务状态
     * @param serverId 服务ID
     * @param status 服务状态
     * @return  Boolean
     *
     */
    Boolean modifyByStauts(String serverId,Integer status);


    /**
     * 根据id获取服务信息
     * @param id 主键id
     * @return ServerInfoDto
     *
     */
    ServerInfoDto getByServerId(String id);


    /**
     * 分页查询服务信息
     * @param pageNum 每页条数
     * @param pageSize 页码
     * @param serverId 服务
     * @return ServerInfoDto
     *
     */
    TxnPage<ServerInfoDto> getAllPage(Integer pageNum, Integer pageSize,String serverId);

    /**
     * 获取非删除状态的服务ID和名称，用于选择的下来菜单
     * @return
     */
    List<ServerInfoDto> getIdAndNameList();


    /**
     * 从注册中心上获取所有服务
     * @return
     */
    List<ServerInfoDto> getAllServerFromEureka();

    /**
     * 是否可以删除网关服务
     *
     * @param serverId 网关服务id
     * @return Boolean
     */
    Boolean isDelete(String serverId);
}

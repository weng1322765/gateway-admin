package jrx.anydmp.gateway.admin.service;

import jrx.anydmp.gateway.dto.FlowLimitDto;
import jrx.anytxn.common.data.TxnPage;

import java.util.List;

/**
 * 限流控制 业务接口
 * @author zhao tingting
 * @date 2018/10/25
 */
public interface IFlowLimitService {

    /**
     * 添加限流信息
     * @param flowLimitDto 限流信息对象
     * @return FlowLimitDto
     *
     */
    FlowLimitDto add(FlowLimitDto flowLimitDto);

    /**
     * 删除限流信息
     * @param  id 主键id
     * @return Boolean
     *
     */
    FlowLimitDto remove(Integer id);


    /**
     * 编辑限流信息
     * @param flowLimitDto 限流信息对象
     * @return  FlowLimitDto
     *
     */
    FlowLimitDto modify(FlowLimitDto flowLimitDto);


    /**
     * 根据serverId获取正在使用中的限流信息
     * @param serverId 服务id
     * @return List<FlowLimitDto>
     *
     */
    List<FlowLimitDto> getByServerId(String serverId);


    /**
     * 根据id获取限流信息
     * @param id 主键id
     * @return RouteLimitDto
     *
     */
    FlowLimitDto getById(Integer id);


    /**
     * 分页查询限流信息
     * @param pageNum 每页条数
     * @param pageSize 页码
     * @return FlowLimitDto
     *
     */
    TxnPage<FlowLimitDto> getAllPage(Integer pageNum, Integer pageSize);

    /**
     * 根据网关和状态分页查询限流信息
     *
     * @param pageNum  每页条数
     * @param pageSize 页码
     * @param serverId 服务Id
     * @param status   状态
     * @return FlowLimitDto
     */
    TxnPage<FlowLimitDto> getPageByServerIdAndStatus(Integer pageNum, Integer pageSize, String serverId, Integer status);

    /**
     * 根据ID编辑限流状态
     * @param id 限流ID
     * @param status 限流状态
     * @return  Boolean
     *
     */
    FlowLimitDto modifyByStauts(Integer id,Integer status);
}

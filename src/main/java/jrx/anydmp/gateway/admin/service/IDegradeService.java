package jrx.anydmp.gateway.admin.service;


import jrx.anydmp.gateway.dto.DegradeDto;
import jrx.anytxn.common.data.TxnPage;

import java.util.List;

/**
 * 降级控制 业务接口
 * @author zhao tingting
 * @date 2018/10/25
 */
public interface IDegradeService {

    /**
     * 添加降级信息
     * @param degradeDto 降级信息对象
     * @return DegradeDto
     *
     */
    DegradeDto add(DegradeDto degradeDto);

    /**
     * 删除降级信息
     * @param  id 主键id
     * @return Boolean
     *
     */
    DegradeDto remove(Integer id);


    /**
     * 编辑降级信息
     * @param degradeDto 降级信息对象
     * @return  DegradeDto
     *
     */
    DegradeDto modify(DegradeDto degradeDto);


    /**
     * 根据id获取降级信息
     * @param id 主键id
     * @return degradeDto
     *
     */
    DegradeDto getById(Integer id);

    /**
     * 分页查询降级信息
     * @param pageNum 每页条数
     * @param pageSize 页码
     * @return DegradeDto
     *
     */
    TxnPage<DegradeDto> getAllPage(Integer pageNum, Integer pageSize);


    /**
     * 根据网关和状态分页查询降级控制信息
     *
     * @param pageNum  每页条数
     * @param pageSize 页码
     * @param serverId 服务Id
     * @param status   状态
     * @return DegradeDto
     */
    TxnPage<DegradeDto> getPageByServerIdAndStatus(Integer pageNum, Integer pageSize, String serverId, Integer status);

    /**
     * 根据ID编辑降级状态
     * @param id 降级ID
     * @param status 降级状态
     * @return  Boolean
     *
     */
    DegradeDto modifyByStauts(Integer id,Integer status);


    /**
     * 根据serverId查询正在使用中的降级信息
     * @param serverId
     * @return
     */
    List<DegradeDto> getByServerId(String serverId);
}

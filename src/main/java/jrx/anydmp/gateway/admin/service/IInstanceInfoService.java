package jrx.anydmp.gateway.admin.service;

import com.netflix.appinfo.InstanceInfo;
import jrx.anydmp.gateway.admin.enums.Status;
import jrx.anydmp.gateway.dto.InstanceInfoDto;
import jrx.anytxn.common.data.TxnPage;

import java.util.List;

/**
 * 实例信息 业务接口
 * @author zhao tingting
 * @date 2018/10/25
 */
public interface IInstanceInfoService {

    /**
     * 添加实例信息
     * @param instanceInfoDto 实例信息对象
     * @return InstanceInfoDto
     *
     */
    InstanceInfoDto add(InstanceInfoDto instanceInfoDto);

    /**
     * 删除实例信息
     * @param  id 主键id
     * @return Boolean
     *
     */
    Boolean remove(Long id);


    /**
     * 编辑实例信息
     * @param instanceInfoDto 实例信息对象
     * @return  InstanceInfoDto
     *
     */
    InstanceInfoDto modify(InstanceInfoDto instanceInfoDto);


    /**
     * 根据serverId获取实例信息
     * @param pageNum 每页条数
     * @param pageSize 页码
     * @param serverId 服务id
     * @return List<InstanceInfoDto>
     *
     */
    TxnPage<InstanceInfoDto> getByServerId(Integer pageNum, Integer pageSize, String serverId);


    /**
     * 根据id获取实例信息
     * @param id 主键id
     * @return InstanceInfoDto
     *
     */
    InstanceInfoDto getById(Long id);


    /**
     * 获取注册中心实例
     * @param serverId
     * @return
     */
    List<InstanceInfo> getInstancesFromEureka(String serverId);


    /**
     * 根据状态获取所有状态为up的实例
     * @return
     */
    List<jrx.anydmp.gateway.entity.InstanceInfo> getUpInstanceList();


}

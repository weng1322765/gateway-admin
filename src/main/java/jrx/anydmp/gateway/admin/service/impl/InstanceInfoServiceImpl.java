package jrx.anydmp.gateway.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import jrx.anydmp.gateway.admin.constant.Constant;
import jrx.anydmp.gateway.admin.enums.InstanceStatus;
import jrx.anydmp.gateway.admin.enums.Status;
import jrx.anydmp.gateway.admin.service.IInstanceInfoService;
import jrx.anydmp.gateway.dto.InstanceInfoDto;
import jrx.anydmp.gateway.entity.InstanceInfo;
import jrx.anydmp.gateway.entity.ServerInfo;
import jrx.anydmp.gateway.mapper.InstanceInfoMapper;
import jrx.anydmp.gateway.mapper.ServerInfoMapper;
import jrx.anytxn.common.data.TxnPage;
import jrx.anytxn.common.exception.TxnArgumentException;
import jrx.anytxn.common.utils.BeanMapping;
import jrx.anytxn.common.utils.TxnStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static jrx.anydmp.gateway.admin.enums.InstanceStatus.UP;


/**
 * 实例信息 业务接口实现
 * @author zhao tingting
 * @date 2018/10/25
 */
@Service
@EnableScheduling
public class InstanceInfoServiceImpl implements IInstanceInfoService{


    private static final Logger logger = LoggerFactory.getLogger(InstanceInfoServiceImpl.class);

    @Autowired
    private EurekaClient eurekaClient;

    @Autowired
    private InstanceInfoMapper instanceInfoMapper;

    @Autowired
    private ServerInfoMapper serverInfoMapper;

    /**
     * 添加实例信息
     *
     * @param instanceInfoDto 实例信息对象
     * @return InstanceInfoDto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InstanceInfoDto add(InstanceInfoDto instanceInfoDto) {
        ServerInfo serverInfo = serverInfoMapper.selectById(instanceInfoDto.getServerId());
        if (serverInfo == null) {
            throw new TxnArgumentException("实例所属的服务不存在");
        }

        InstanceInfo instanceInfo = BeanMapping.copy(instanceInfoDto, InstanceInfo.class);
        instanceInfo.setCreateTime(LocalDateTime.now());
        instanceInfo.setUpdateTime(LocalDateTime.now());
        instanceInfo.setUpdateBy(Constant.DEFAULT_USER);
        instanceInfoMapper.insert(instanceInfo);
        return instanceInfoDto;
    }

    /**
     * 删除实例信息
     *
     * @param id 主键id
     * @return Boolean
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean remove(Long id) {
        InstanceInfo info = instanceInfoMapper.selectById(id);
        if (info == null) {
            throw new TxnArgumentException("需要删除的实例不存在");
        }

        // 实例状态为启用状态下不可删除
        if (info.getStatus().equals(Status.ENABLE.getCode())) {
            throw new TxnArgumentException("状态为启用时不可删除");
        }
        return instanceInfoMapper.deleteById(id) == 1;
    }

    /**
     * 编辑实例信息
     *
     * @param instanceInfoDto 实例信息对象
     * @return InstanceInfoDto
     */
    @Override
    public InstanceInfoDto modify(InstanceInfoDto instanceInfoDto) {
        InstanceInfo info = instanceInfoMapper.selectById(instanceInfoDto.getId());
        if (info == null) {
            throw new TxnArgumentException("需要更新的实例不存在");
        }
        InstanceInfo instanceInfo = BeanMapping.copy(instanceInfoDto, InstanceInfo.class);
        instanceInfo.setUpdateTime(LocalDateTime.now());
        instanceInfoMapper.updateById(instanceInfo);
        return BeanMapping.copy(instanceInfo,InstanceInfoDto.class);
    }


    /**
     * 根据serverId获取实例信息
     *
     * @param pageNum  每页条数
     * @param pageSize 页码
     * @param serverId 服务id
     * @return List<InstanceInfoDto>
     */
    @Override
    public TxnPage<InstanceInfoDto> getByServerId(Integer pageNum, Integer pageSize, String serverId) {
        QueryWrapper wrapper = new QueryWrapper();
        if (!StringUtils.isEmpty(serverId)) {
            wrapper.eq("SERVER_ID", serverId);
        }
        wrapper.eq("STATUS", UP);
        Page<InstanceInfo> page = new Page(pageNum, pageSize);
        instanceInfoMapper.selectPage(page, wrapper);

        List<InstanceInfoDto> dtoList = BeanMapping.copyList(page.getRecords(), InstanceInfoDto.class);
        return new TxnPage(pageNum, pageSize, page.getTotal(), dtoList);

    }

    /**
     * 根据id获取实例信息
     *
     * @param id 主键id
     * @return InstanceInfoDto
     */
    @Override
    public InstanceInfoDto getById(Long id) {
        if (StringUtils.isEmpty(id)) {
            throw new TxnArgumentException("实例Id不可为空");
        }
        InstanceInfo info = instanceInfoMapper.selectById(id);
        return BeanMapping.copy(info,InstanceInfoDto.class);
    }


    /**
     * 从注册中心检查实例
     */
    @Scheduled(initialDelay = 10*1000L, fixedDelay = 10*1000L)
    public void checkInstance() {
        //获取服务列表
        List<ServerInfo> list = serverInfoMapper.selectList(new QueryWrapper<ServerInfo>().eq("status",1));
        if(list!=null && !list.isEmpty()) {
            for (ServerInfo info : list) {
                this.refreshInstance(info.getServerId());
            }
        }

    }

    public void refreshInstance(String serverName) {
        if(StringUtils.isEmpty(serverName)){
            logger.warn("服务名称为空");
            return;
        }

        List<com.netflix.appinfo.InstanceInfo> instanceList = null;

        Applications applications = eurekaClient.getApplications();
        Application app = applications.getRegisteredApplications(serverName);
        if(app==null){
            logger.warn("服务:{} 在注册中心不存在",serverName);
        }else{
            instanceList = app.getInstancesAsIsFromEureka();
            if(instanceList==null || instanceList.isEmpty()){
                logger.warn("服务{} 在注册中心不存在实例",serverName);
            }
        }

        if(instanceList==null || instanceList.isEmpty()){
            //通过serverId 更新所有实例为up为down
            instanceInfoMapper.updateStatusByServerId(serverName, InstanceStatus.DOWN.name(), UP.name());
            return;
        }

        //数据库中的示例转成列表
        Map<Long,InstanceInfo> instanceInfoMap = new HashMap<>();
        List<InstanceInfo> instanceInfos = instanceInfoMapper.selectByServerId(serverName);
        if(instanceInfos!=null && !instanceInfos.isEmpty()){
            for (InstanceInfo ii : instanceInfos) {
                instanceInfoMap.put(ii.getId(),ii);
            }
        }

        for (com.netflix.appinfo.InstanceInfo instance : instanceList){
            long instanceId = TxnStringUtils.getCRC32(instance.getInstanceId());
            //InstanceInfo instanceInfo = instanceInfoMapper.selectById(instanceId);
            InstanceInfo instanceInfo = instanceInfoMap.get(instanceId);
            if(instanceInfo!=null){
                if(!instanceInfo.getStatus().equals(instance.getStatus().name())
                        || System.currentTimeMillis()-instanceInfo.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() > 60*1000){
                    instanceInfo.setHostName(instance.getHostName());
                    instanceInfo.setIp(instance.getIPAddr());
                    instanceInfo.setPort(instance.getPort());
                    instanceInfo.setStatus(instance.getStatus().toString());
                    instanceInfo.setUpdateTime(LocalDateTime.now());
                    instanceInfoMapper.updateById(instanceInfo);
                }
                //更新完状态以后移除
                instanceInfoMap.remove(instanceId);
            }else{
                instanceInfo = new InstanceInfo();
                instanceInfo.setId(instanceId);
                instanceInfo.setServerId(instance.getAppName());
                instanceInfo.setInstanceName(instance.getInstanceId());
                instanceInfo.setHostName(instance.getHostName());
                instanceInfo.setIp(instance.getIPAddr());
                instanceInfo.setStatus(instance.getStatus().toString());
                instanceInfo.setPort(instance.getPort());
                instanceInfo.setCreateTime(LocalDateTime.now());
                instanceInfo.setUpdateTime(LocalDateTime.now());
                instanceInfoMapper.insert(instanceInfo);
            }
        }

        //将注册中心不存在的示例改为down
        if(instanceInfoMap.size()>0){
            for (InstanceInfo instance : instanceInfoMap.values()){
                if(UP.name().equals(instance.getStatus())){
                    instance.setStatus(InstanceStatus.DOWN.name());
                    instance.setUpdateTime(LocalDateTime.now());
                    instanceInfoMapper.updateById(instance);
                }
            }

        }
    }


    @Override
    public List<com.netflix.appinfo.InstanceInfo> getInstancesFromEureka(String serverId) {
        Application app = eurekaClient.getApplications().getRegisteredApplications(serverId);
        if (app == null) {
            logger.warn("服务:{} 在注册中心不存在", serverId);
            return null;
        }
        List<com.netflix.appinfo.InstanceInfo> list = app.getInstancesAsIsFromEureka().stream().filter(instanceInfo
                -> com.netflix.appinfo.InstanceInfo.InstanceStatus.UP.name().equals(instanceInfo.getStatus().name()))
                .collect(Collectors.toList());
        return list;
    }


    @Override
    public List<InstanceInfo> getUpInstanceList() {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("status", "UP");
        return instanceInfoMapper.selectList(qw);
    }
}

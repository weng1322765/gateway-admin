package jrx.anydmp.gateway.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netflix.appinfo.InstanceInfo;
import jrx.anydmp.gateway.admin.dto.OperateResult;
import jrx.anydmp.gateway.admin.service.IInstanceInfoService;
import jrx.anydmp.gateway.admin.service.IOperateService;
import jrx.anydmp.gateway.dto.OperateEventDto;
import jrx.anydmp.gateway.entity.OperateEvent;
import jrx.anydmp.gateway.mapper.OperateEventMapper;
import jrx.anytxn.common.data.TxnPage;
import jrx.anytxn.common.data.TxnRespCode;
import jrx.anytxn.common.data.TxnRespResult;
import jrx.anytxn.common.exception.TxnArgumentException;
import jrx.anytxn.common.utils.BeanMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class OperateServiceImpl implements IOperateService {
    private static final Logger logger = LoggerFactory.getLogger(OperateServiceImpl.class);

    @Autowired
    private OperateEventMapper operateEventMapper;

    @Autowired
    private IInstanceInfoService instanceInfoService;


    @Override
    public OperateResult formatOperateEvent(OperateEvent operateEvent) {
        OperateResult result = new OperateResult();
        int inst = 0;
        List<InstanceInfo> instanceList = instanceInfoService.getInstancesFromEureka(operateEvent.getServerId());
        if (instanceList == null || instanceList.size() == 0) {
            logger.warn("该{}服务下没有可用实例！", operateEvent.getServerId());
            return null;
        }
        inst = instanceList.size();

        List<OperateEvent> eventList = new ArrayList<>();
        for (InstanceInfo instanceInfo : instanceList) {
            operateEvent.setInstanceHomePage(instanceInfo.getHomePageUrl());
            operateEventMapper.insertReturnId(operateEvent);
            eventList.add(operateEvent);
        }
        result.setInstance(inst);
        result.setOperateEventList(eventList);
        return result;
    }


    @Override
    public OperateResult informGateway(List<OperateEvent> eventList, OperateResult operateResult) {
        int success = 0;
        int failed = 0;
        for (OperateEvent event : eventList) {
            RestTemplate restTemplate = new RestTemplate();
            TxnRespResult response = null;
            try {
                String result = restTemplate.getForObject(event.getInstanceHomePage() + "operate/event?id=" + event.getId(), String.class);
                response = JSON.parseObject(result, TxnRespResult.class);
            } catch (Exception e) {
                logger.error("系统异常！", e);
            }
            if (response != null && response.getCode() == TxnRespCode.SUCCESS.getCode()) {
                success++;
            } else {
                logger.warn("实例{}同步数据失败", event.getInstanceHomePage());
                failed++;
            }
        }
        operateResult.setSuccess(success);
        operateResult.setFailed(failed);
        return operateResult;
    }

    /**
     * 分页获取操作记录信息
     *
     * @param user     操作用户
     * @param type     操作类型
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public TxnPage<OperateEventDto> getPageByUserAndType(String user, String type, Integer pageNum, Integer pageSize) {
        Page<OperateEvent> page = new Page(pageNum, pageSize);
        QueryWrapper wrapper = new QueryWrapper();
        if (!StringUtils.isEmpty(user)) {
            wrapper.like("update_by", user);
        }

        if (!StringUtils.isEmpty(type)) {
            wrapper.eq("type", type);
        }
        wrapper.orderByDesc("id");
        operateEventMapper.selectPage(page, wrapper);
        List<OperateEventDto> dtoList = BeanMapping.copyList(page.getRecords(), OperateEventDto.class);

        return new TxnPage(pageNum, pageSize, page.getTotal(), dtoList);
    }

    /**
     * 根据id获取操作记录信息
     *
     * @param id 主键id
     * @return OperateEventDto
     */
    @Override
    public OperateEventDto getById(Integer id) {
        if (StringUtils.isEmpty(id)) {
            throw new TxnArgumentException("Id不可为空");
        }
        OperateEvent operateEvent = operateEventMapper.selectById(id);
        return BeanMapping.copy(operateEvent,OperateEventDto.class);
    }
}

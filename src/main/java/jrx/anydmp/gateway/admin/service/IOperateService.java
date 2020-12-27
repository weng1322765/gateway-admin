package jrx.anydmp.gateway.admin.service;

import jrx.anydmp.gateway.admin.dto.OperateResult;
import jrx.anydmp.gateway.dto.OperateEventDto;
import jrx.anydmp.gateway.entity.OperateEvent;
import jrx.anytxn.common.data.TxnPage;

import java.util.List;
import java.util.Map;

public interface IOperateService {

    OperateResult formatOperateEvent(OperateEvent operateEvent);

    OperateResult informGateway(List<OperateEvent> eventList, OperateResult operateResult);


    /**
     * 分页获取操作记录信息
     * @param user 操作用户
     * @param type 操作类型
     * @param pageNum
     * @param pageSize
     * @return
     */
    TxnPage<OperateEventDto> getPageByUserAndType(String user, String type, Integer pageNum, Integer pageSize);


    /**
     * 根据id获取操作记录信息
     * @param id 主键id
     * @return OperateEventDto
     *
     */
    OperateEventDto getById(Integer id);

}

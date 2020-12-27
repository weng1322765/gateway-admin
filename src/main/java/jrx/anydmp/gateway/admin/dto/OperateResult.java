package jrx.anydmp.gateway.admin.dto;

import jrx.anydmp.gateway.entity.OperateEvent;


import java.io.Serializable;
import java.util.List;

public class OperateResult implements Serializable {

    private static final long serialVersionUID = -114971594955984667L;
    private int instance;
    private int success;
    private int failed;
    private List<OperateEvent> operateEventList;


    public int getInstance() {
        return instance;
    }

    public void setInstance(int instance) {
        this.instance = instance;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public List<OperateEvent> getOperateEventList() {
        return operateEventList;
    }

    public void setOperateEventList(List<OperateEvent> operateEventList) {
        this.operateEventList = operateEventList;
    }
}

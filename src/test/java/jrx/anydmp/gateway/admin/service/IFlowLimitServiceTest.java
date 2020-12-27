package jrx.anydmp.gateway.admin.service;

import jrx.anydmp.gateway.dto.FlowLimitDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author zhao tingting
 * @date 2018/10/29
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class IFlowLimitServiceTest {

    @Autowired
    private IFlowLimitService flowLimitService;

    @Test
    public void add() {
        FlowLimitDto flowLimitDto = new FlowLimitDto();
        flowLimitDto.setName("lalallal");
        flowLimitDto.setFlowControlMethod(1);
        flowLimitDto.setFlowControlMode(1);
        flowLimitService.add(flowLimitDto);

    }

    @Test
    public void remove() {
    }

    @Test
    public void modify() {
    }

    @Test
    public void getByServerId() {
    }

    @Test
    public void getById() {
    }
}
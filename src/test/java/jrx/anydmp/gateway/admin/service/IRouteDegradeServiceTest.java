package jrx.anydmp.gateway.admin.service;

import jrx.anydmp.gateway.dto.DegradeDto;
import org.junit.Assert;
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
public class IRouteDegradeServiceTest {

    @Autowired
    private IDegradeService degradeService;

    @Test
    public void add() {
        DegradeDto routeDegradeDto = new DegradeDto();
        routeDegradeDto.setName("降级");
        routeDegradeDto.setPeriod(2);
        routeDegradeDto.setRouteId(1);
        routeDegradeDto.setServerId("aaaa");
        routeDegradeDto.setThresholdType(1);
        routeDegradeDto.setThresholdValue(1.0);
        DegradeDto result = degradeService.add(routeDegradeDto);
        Assert.assertNotNull(result.getCreateTime());
    }

    @Test
    public void remove() {
    }

    @Test
    public void modify() {
//        RouteDegradeDto routeDegradeDto = new RouteDegradeDto();
//        routeDegradeDto.setName();
//        routeDegradeDto.setPeriod();
//        routeDegradeDto.setRouteId();
//        routeDegradeDto.setServerId();
//        routeDegradeDto.setThresholdType();
//        routeDegradeDto.setRt();
//        RouteDegradeDto result = routeDegradeService.modify(routeDegradeDto);
//        Assert.assertEquals(result.getUpdateTime().compareTo());

    }

    @Test
    public void getById() {
//        routeDegradeService.getById();


    }
}
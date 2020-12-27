package jrx.anydmp.gateway.admin.service;

import jrx.anydmp.gateway.dto.RouteInfoDto;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author zhao tingting
 * @date 2018/10/29
 */
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class IRouteInfoServiceTest {

    @Autowired
    private IRouteInfoService routeInfoService;

    @Test
    public void add() {
        RouteInfoDto routeInfoDto=new RouteInfoDto();
        routeInfoDto.setServerId("APPLICATION-GATEWAY");
        routeInfoDto.setRouteName("route");
        routeInfoDto.setRouteUrl("10.0.9.10");
        routeInfoDto.setInterfaceUrl("http://localhost:9102/param/exception/runtime");
        routeInfoDto.setRules("aaaaaaa");
        RouteInfoDto result = routeInfoService.add(routeInfoDto);

        Assert.assertNotNull(result.getCreateTime());


    }

    @Test
    public void remove() {
        routeInfoService.remove(2);
    }

    @Test
    public void modify() {
        RouteInfoDto routeInfoDto = new RouteInfoDto();
        routeInfoDto.setId(1);
        routeInfoDto.setServerId("APPLICATION-GATEWAY1");
        routeInfoDto.setRouteName("route1");
        routeInfoDto.setRouteUrl("10.0.9.1");
        routeInfoDto.setInterfaceUrl("http://localhost:9103/param/exception/runtime");
        routeInfoDto.setRules("aaaaaaa111");
        RouteInfoDto result = routeInfoService.modify(routeInfoDto);

        Assert.assertNotNull(result.getUpdateTime());
    }

    @Test
    public void getByServerId() {
        List<RouteInfoDto> result = routeInfoService.getByServerId("APPLICATION-GATEWAY");
        Assert.assertNotNull(result);
    }

    @Test
    public void getById() {
        RouteInfoDto result = routeInfoService.getById(1);
        Assert.assertNotNull(result);
    }
}
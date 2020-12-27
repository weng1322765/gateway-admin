package jrx.anydmp.gateway.admin.service;

import jrx.anydmp.gateway.dto.InstanceInfoDto;
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
public class IInstanceInfoServiceTest {

    @Autowired
    private IInstanceInfoService instanceInfoService;

    @Test
    public void add() {
        InstanceInfoDto infoDto = new InstanceInfoDto();
        infoDto.setInstanceName("instance2");
        infoDto.setServerId("APPLICATION-GATEWAY");
        infoDto.setDescription("实例测试");
        infoDto.setHostName("SERVER-1");
        infoDto.setIp("192.168.111.116");
        infoDto.setPort(9101);
        InstanceInfoDto result = instanceInfoService.add(infoDto);
        Assert.assertNotNull(result);
    }

    @Test
    public void remove() {
        boolean flag = instanceInfoService.remove(1056839056639467522L);
        Assert.assertEquals(flag,true);
    }

    @Test
    public void modify() {
        InstanceInfoDto infoDto = new InstanceInfoDto();
        infoDto.setId(1056834350001274881L);
        infoDto.setInstanceName("instance3");
        infoDto.setServerId("APPLICATION-GATEWAY");
        infoDto.setDescription("实例测试test");
        infoDto.setHostName("lenovo");
        infoDto.setIp("192.168.111.115");
        infoDto.setPort(9102);
        InstanceInfoDto result = instanceInfoService.modify(infoDto);
        Assert.assertNotNull(result);
    }

//    @Test
//    public void getByServerId() {
//        List<InstanceInfoDto> infoDtoList = instanceInfoService.getByServerId("APPLICATION-GATEWAY");
//        Assert.assertNotNull(infoDtoList.size());
//    }

    @Test
    public void getById() {
        InstanceInfoDto infoDto = instanceInfoService.getById(1056834350001274881L);
        Assert.assertNotNull(infoDto);

    }
}
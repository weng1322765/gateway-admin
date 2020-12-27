package jrx.anydmp.gateway.admin.service;

import jrx.anydmp.gateway.dto.ServerInfoDto;
import jrx.anydmp.gateway.entity.ServerInfo;
import jrx.anytxn.common.utils.BeanMapping;
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
public class IServerInfoServiceTest {

    @Autowired
    private IServerInfoService serverInfoService;

    @Test
    public void add() {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setServerId("APPLICATION-GATEWAY1");
        serverInfo.setServerName("应用");
        serverInfo.setMinInstanceAmt(2);
        serverInfo.setDescription("测试");
        ServerInfoDto serverInfoDto = BeanMapping.copy(serverInfo, ServerInfoDto.class);
        ServerInfoDto result = serverInfoService.add(serverInfoDto);
        Assert.assertNotNull(result.getCreateTime());

    }

    @Test
    public void remove() {
        boolean flag = serverInfoService.remove("APPLICATION-GATEWAY1");
        Assert.assertEquals(flag,true);


    }

    @Test
    public void modify() {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setServerId("APPLICATION-GATEWAY");
        serverInfo.setServerName("应用test");
        serverInfo.setMinInstanceAmt(3);
        serverInfo.setDescription("测试test");
        ServerInfoDto old = serverInfoService.getByServerId("APPLICATION-GATEWAY");

        ServerInfoDto serverInfoDto = BeanMapping.copy(serverInfo, ServerInfoDto.class);
        ServerInfoDto result = serverInfoService.modify(serverInfoDto);
        Assert.assertEquals(result.getUpdateTime().compareTo(old.getUpdateTime()) == 1,true);
    }

    @Test
    public void getByServerId() {
        ServerInfoDto serverInfo = serverInfoService.getByServerId("APPLICATION-GATEWAY");
        Assert.assertNotNull(serverInfo);
    }
}
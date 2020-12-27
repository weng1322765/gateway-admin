package jrx.anydmp.gateway;


import jrx.anytxn.sadmin.client.config.EnableAdminClient;
import jrx.auth.filter.config.EnableAuthFilter;
import jrx.common.web.config.EnableWebConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 服务注册中心启动类
 *
 * @author zwg
 * @date 2018-03-06 15:48
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAdminClient
@EnableAuthFilter
@EnableWebConfig
@MapperScan("jrx.anydmp.gateway.mapper")
public class GatewayAdminApplication {

    public static void main(String[] args) {
        new SpringApplication(GatewayAdminApplication.class).run(args);
    }

}

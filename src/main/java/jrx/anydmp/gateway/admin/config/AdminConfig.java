package jrx.anydmp.gateway.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;



/**
 * 配置类
 *
 * @author zwg
 * @date 2018-09-28 10:19
 **/

@Configuration
public class AdminConfig {


    @Bean
    public RestTemplate instanceMertricsRestTemplate(){
        RestTemplate  rt = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        //milliseconds
        requestFactory.setReadTimeout(1000);
        //milliseconds
        requestFactory.setConnectTimeout(1000);

        rt.setRequestFactory(requestFactory);
        return rt;
    }


}

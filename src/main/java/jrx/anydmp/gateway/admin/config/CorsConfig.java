package jrx.anydmp.gateway.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置文件
 *
 * @author zwg
 * @date 2018-08-29 10:39
 **/

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") //允许任何域名使用
                .allowedHeaders("*") //允许任何头
                .allowedMethods("*") //允许任何方法  或者 "GET", "POST", "DELETE", "PUT" 等
                .allowCredentials(true) //允许证书
                .maxAge(3600L); //最大时间
    }
}

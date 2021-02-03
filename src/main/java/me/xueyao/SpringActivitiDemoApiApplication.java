package me.xueyao;

import org.activiti.core.common.spring.identity.config.ActivitiSpringIdentityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


/**
 * 
 * @author simonxue
 */
@SpringBootApplication(exclude = {
        // 排除SpringSecurity的自动配置
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class,
        ActivitiSpringIdentityAutoConfiguration.class
})
public class SpringActivitiDemoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringActivitiDemoApiApplication.class, args);
    }


}

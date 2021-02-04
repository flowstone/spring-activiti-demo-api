package me.xueyao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * 
 * @author simonxue
 */
@SpringBootApplication(exclude = {
        //DataSourceAutoConfiguration.class,
        org.activiti.spring.boot.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class

})
@MapperScan(basePackages = "me.xueyao.mapper")
public class SpringActivitiDemoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringActivitiDemoApiApplication.class, args);
    }


}

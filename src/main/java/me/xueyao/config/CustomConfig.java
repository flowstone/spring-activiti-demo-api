package me.xueyao.config;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author Simon.Xue
 * @date 2/4/21 2:16 PM
 **/
//@Component
//@Getter
public class CustomConfig {
    @Value("${custom.datasource.url}")
    private String url;
    @Value("${custom.datasource.driverClassName}")
    private String driverClassName;
    @Value("${custom.datasource.username}")
    private String username;
    @Value("${custom.datasource.password}")
    private String password;
}

package me.xueyao.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * @author Simon.Xue
 * @date 2/4/21 1:37 PM
 **/
//@Configuration
//@MapperScan(basePackages = {"me.xueyao.mapper"},sqlSessionFactoryRef = "baseSqlSessionFactory")
public class DataSourceConfig {

    @Autowired
    private CustomConfig customConfig;
    /*@Bean(name = "baseDataSource")
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(customConfig.getDriverClassName());
        ds.setUrl(customConfig.getUrl());
        ds.setUsername(customConfig.getUsername());
        ds.setPassword(customConfig.getPassword());
        return ds;
    }*/

    /**
     * 注入session工厂
     *
     * @param dataSource dataSource
     * @return SqlSessionFactory
     * @throws Exception 异常
     */
    @Bean(name = "baseSqlSessionFactory")
    public SqlSessionFactory buildSqlSessionFactory(@Qualifier("baseDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("mapper/*.xml"));
        return sessionFactory.getObject();
    }


    /**
     * 注入事务管理器
     *
     * @param dataSource dataSource
     * @return DataSourceTransactionManager
     */
    @Bean(name = "baseTransactionManager")
    public DataSourceTransactionManager buildTransactionManager(@Qualifier("baseDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
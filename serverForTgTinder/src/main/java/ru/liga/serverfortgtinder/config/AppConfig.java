package ru.liga.serverfortgtinder.config;

import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@Slf4j
public class AppConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(DriverManagerDataSource dataSource){
        log.debug("Создаем jdbc");
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public DriverManagerDataSource dataSource(){
        log.debug("Создаем драйвер подключения к БД");
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl("jdbc:postgresql://localhost:5432/league_db");
        ds.setUsername("admin");
        ds.setPassword("12345");
        log.debug("Закончили создавать драйвер подключения к БД");
        return ds;
    }
    @Bean
    public SpringLiquibase springLiquibase(){
        log.debug("Создаем liquibase");
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource());
        liquibase.setChangeLog("classpath:/db/changelog-master.yaml");
        log.debug("Закончили создавать liquibase");
        return liquibase;
    }

}

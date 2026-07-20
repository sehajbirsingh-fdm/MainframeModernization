package com.bankofz.inqcust.api.inqacc.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "app.data.mode", havingValue = "db")
public class InqaccDataSourceConfiguration {

    @Bean
    public DataSource inqaccDataSource(
            @Value("${app.inqacc.db.url:}") String url,
            @Value("${app.inqacc.db.username:}") String username,
            @Value("${app.inqacc.db.password:}") String password,
            @Value("${app.inqacc.db.driver-class-name:}") String driverClassName,
            @Value("${app.inqacc.db.maximum-pool-size:10}") int maximumPoolSize,
            @Value("${app.inqacc.db.minimum-idle:2}") int minimumIdle
    ) {
        require(url, "app.inqacc.db.url");
        require(username, "app.inqacc.db.username");
        require(password, "app.inqacc.db.password");

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url.trim());
        dataSource.setUsername(username.trim());
        dataSource.setPassword(password);
        if (!driverClassName.isBlank()) {
            dataSource.setDriverClassName(driverClassName.trim());
        }
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        return dataSource;
    }

    private void require(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(propertyName + " is required when app.data.mode=db");
        }
    }
}

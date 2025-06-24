package com.accesshr.emsbackend.EmployeeController.Config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Component
public class CountryDataSourceManager {

    private final Map<String, DataSource> dataSources = new HashMap<>();

    @PostConstruct
    public void init() {
        dataSources.put("UK", createDataSource("talent-flow-server-uk.mysql.database.azure.com", "mtl", "mtl@123456"));
        dataSources.put("INDIA", createDataSource("talent-flow-server-db-server.mysql.database.azure.com", "mtl", "mtl@123456"));
    }

    private DataSource createDataSource(String url, String username, String password) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:mysql://" + url + ":3306");
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return ds;
    }

    public DataSource getDataSourceForCountry(String country) {
        return dataSources.get(country.toUpperCase());
    }
}


package com.accesshr.emsbackend.EmployeeController.Config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CountryDataSourceManager {

    private final static Logger logger= LoggerFactory.getLogger(CountryDataSourceManager.class);

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

//        ds.setMaximumPoolSize(20);
//        ds.setMinimumIdle(5);
//        ds.setConnectionTimeout(30000); // 30 sec
//        ds.setIdleTimeout(600000);      // 10 min
//        ds.setMaxLifetime(1800000);
        return ds;
    }

    public DataSource getDataSourceForCountry(String country) {
        return dataSources.get(country.toUpperCase());
    }

    // Fetch all schemas from INFORMATION_SCHEMA for a given country.
    public List<String> getAllSchemas(String country) {
        DataSource ds = getDataSourceForCountry(country);
        List<String> schemas = new ArrayList<>();

        String sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA " +
                "WHERE SCHEMA_NAME NOT IN ('mysql','information_schema','performance_schema','sys')";

        try (Connection connection = ds.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                schemas.add(rs.getString("SCHEMA_NAME"));
            }

        } catch (SQLException e) {
            logger.error("Failed to fetch schemas for country: {}", country, e);
            throw new RuntimeException("Failed to fetch all schemas: " + e.getMessage(), e);
        }

        return schemas;
    }

    // Fetch all schemas across all countries.
    public Map<String, List<String>> getAllSchemasAcrossCountries(){
        Map<String, List<String>> result=new HashMap<>();
        for(String country: dataSources.keySet()){
            result.put(country, getAllSchemas(country));
        }
        return result;
    }
}


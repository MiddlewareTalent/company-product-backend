package com.accesshr.emsbackend.Service;

import com.accesshr.emsbackend.EmployeeController.Config.CountryDataSourceManager;
import com.accesshr.emsbackend.EmployeeController.Config.DataSourceConfig;
import com.accesshr.emsbackend.Entity.*;
import com.accesshr.emsbackend.Entity.Invoice.Client;
import com.accesshr.emsbackend.Entity.Invoice.CompanyDetails;
import com.accesshr.emsbackend.Entity.Invoice.Invoice;
import com.accesshr.emsbackend.Entity.Invoice.InvoiceItem;
import com.accesshr.emsbackend.exceptions.ResourceNotFoundException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TenantSchemaService {

    private final CountryDataSourceManager countryDataSourceManager;

    private final DataSourceConfig dataSourceConfig;

    public TenantSchemaService(CountryDataSourceManager countryDataSourceManager, DataSourceConfig dataSourceConfig) {
        this.countryDataSourceManager = countryDataSourceManager;
        this.dataSourceConfig = dataSourceConfig;
    }

    public void createTenant(String schemaName, String country) throws SQLException {
        schemaName = schemaName.replace(" ", "_");
        CountryServerConfig config = CountryServerConfig.valueOf(country.toUpperCase());

        DataSource countryDataSource = countryDataSourceManager.getDataSourceForCountry(country);
        if (schemaExistsAndHasData(countryDataSource, schemaName)) {
            throw new ResourceNotFoundException("Schema '" + schemaName + "' already exists and contains data.");
        }

        if (!publicSchemaExistsOrNot(config)) {
            createSchemaIfNotExists("public", config);
            createTablesInPublicSchema("public", config);
        }

        createSchemaIfNotExists(schemaName, config);
        createTablesInSchema(schemaName, config);
    }

    private boolean schemaExistsAndHasData(DataSource dataSource, String schemaName) throws SQLException {
        String checkTablesSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(checkTablesSql)) {
            stmt.setString(1, schemaName);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("Schema Exists AndHasData: "+rs);
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private boolean publicSchemaExistsOrNot(CountryServerConfig config) {
        String jdbcUrl = "jdbc:mysql://" + config.getServerUrl() + ":3306/" +
                "?useSSL=true&allowPublicKeyRetrieval=true";
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dataSourceConfig.getDbUsername(), dataSourceConfig.getDbPassword());
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "public");
            ResultSet rs = stmt.executeQuery();
            System.out.println("Result set: "+rs);
            if (rs.next()) {
                return rs.getInt(1) > 0; // true if schema exists
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Failed to check schema on " + config.getServerUrl());
            e.printStackTrace();
            return false;
        }
    }


    private void createSchemaIfNotExists(String schemaName, CountryServerConfig config) throws SQLException {
        String jdbcUrl = "jdbc:mysql://" + config.getServerUrl() + ":3306/" +
                "?useSSL=true&allowPublicKeyRetrieval=true";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dataSourceConfig.getDbUsername(), dataSourceConfig.getDbPassword());
             Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS `" + schemaName + "`");
        } catch (SQLException e) {
            throw new SQLException("Schema creation failed on " + config.getServerUrl(), e);
        }
    }

    private void createTablesInSchema(String schemaName, CountryServerConfig config) {
        Map<String, Object> settings = new HashMap<>();

        settings.put("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        settings.put("hibernate.connection.url", "jdbc:mysql://" + config.getServerUrl() + ":3306/" + schemaName);
        settings.put("hibernate.connection.username", dataSourceConfig.getDbUsername());
        settings.put("hibernate.connection.password", dataSourceConfig.getDbPassword());
        settings.put("hibernate.hbm2ddl.auto", "update");
        settings.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(settings)
                .build();

        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
        List<Class<?>> entityClasses = Arrays.asList(
                Timesheet.class,
                Task.class,
                CompanyNews.class,
                Contacts.class,
                EmployeeManager.class,
                Holiday.class,
                LeaveRequest.class,
                LeaveSheet.class,
                Notifications.class,
                JobRoles.class,
                Client.class,
                CompanyDetails.class,
                Invoice.class,
                InvoiceItem.class
        );

        for (Class<?> entityClass : entityClasses) {
            metadataSources.addAnnotatedClass(entityClass);
        }

        Metadata metadata = metadataSources.buildMetadata();

        SchemaManagementToolCoordinator.process(
                metadata,
                serviceRegistry,
                settings,
                null
        );
    }


    private void createTablesInPublicSchema(String schemaName, CountryServerConfig config) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        settings.put("hibernate.connection.url", "jdbc:mysql://" + config.getServerUrl() + ":3306/" + schemaName);
        settings.put("hibernate.connection.username", dataSourceConfig.getDbUsername());
        settings.put("hibernate.connection.password", dataSourceConfig.getDbPassword());
        settings.put("hibernate.hbm2ddl.auto", "create");
        settings.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");

        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(settings)
                .build();

        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
        List<Class<?>> entityClasses = Arrays.asList(
                Timesheet.class,
                Task.class,
                CompanyNews.class,
                Contacts.class,
                EmployeeManager.class,
                Holiday.class,
                LeaveRequest.class,
                LeaveSheet.class,
                Notifications.class,
                JobRoles.class,
                ClientDetails.class,
                Client.class,
                CompanyDetails.class,
                Invoice.class,
                InvoiceItem.class
        );

        for (Class<?> entityClass : entityClasses) {
            metadataSources.addAnnotatedClass(entityClass);
        }

        Metadata metadata = metadataSources.buildMetadata();

        SchemaManagementToolCoordinator.process(
                metadata,
                serviceRegistry,
                settings,
                null
        );
    }
}
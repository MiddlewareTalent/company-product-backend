package com.accesshr.emsbackend.Service;

import com.accesshr.emsbackend.Entity.*;
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

    private final DataSource dataSource;

    public TenantSchemaService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createTenant(String schemaName) throws SQLException {
        schemaName = schemaName.replace(" ", "_");
        if (schemaExistsAndHasData(schemaName)) {
            throw new ResourceNotFoundException("Schema '" + schemaName + "' already exists and contains data.");
        }
        createSchemaIfNotExists(schemaName);
        createTablesInSchema(schemaName);
    }

    private boolean schemaExistsAndHasData(String schemaName) throws SQLException {
        String checkTablesSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(checkTablesSql)) {
            stmt.setString(1, schemaName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int tableCount = rs.getInt(1);
                    return tableCount > 0;
                }
            }
        }
        return false;
    }

    private void createSchemaIfNotExists(String schemaName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS `" + schemaName + "`");
        }catch (SQLException e) {
            throw new SQLException("Schema not created"+e.getMessage());
        }
    }

    private void createTablesInSchema(String schemaName) {
        // Hibernate properties for the new schema
        Map<String, Object> settings = new HashMap<>();
        settings.put("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
        settings.put("hibernate.connection.url", "jdbc:mysql://talent-flow-server-db-server.mysql.database.azure.com:3306/" + schemaName);
        settings.put("hibernate.connection.username", "mtl");
        settings.put("hibernate.connection.password", "mtl@123456");
        settings.put("hibernate.hbm2ddl.auto", "create");

        // Build service registry
        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(settings)
                .build();

        // Register entity classes
        MetadataSources metadataSources = new MetadataSources(serviceRegistry);

        List<Class<?>> entityClasses= Arrays.asList(
                Timesheet.class,
                Task.class,
                CompanyNews.class,
                Contacts.class,
                EmployeeManager.class,
                Holiday.class,
                LeaveRequest.class,
                LeaveSheet.class,
                Notifications.class,
                JobRoles.class

        );
        for (Class<?> entityClass : entityClasses) {
            metadataSources.addAnnotatedClass(entityClass);
        }
//        metadataSources.addAnnotatedClass(Timesheet.class); // Add more entities as needed

        // Build metadata
        Metadata metadata = metadataSources.buildMetadata();

        // Use SchemaManagementToolCoordinator to create tables
        SchemaManagementToolCoordinator.process(
                metadata,
                serviceRegistry,
                settings,
                null
        );
    }

}

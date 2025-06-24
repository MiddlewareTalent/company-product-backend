package com.accesshr.emsbackend.EmployeeController.Config;

import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider {

    private final DataSource dataSource;
    private final CountryDataSourceManager dataSourceManager;

    public SchemaMultiTenantConnectionProvider(DataSource dataSource, CountryDataSourceManager dataSourceManager) {
        this.dataSource = dataSource;
        this.dataSourceManager = dataSourceManager;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(Object tenantIdentifier) throws SQLException {
        String schema = tenantIdentifier.toString();

//        if ("public".equalsIgnoreCase(schema)) {
//            // Use default datasource for public schema
//            Connection conn = dataSource.getConnection();
//            conn.createStatement().execute("USE public");
//            return conn;
//        }
        String country = TenantContext.getCountry(); // new ThreadLocal
        if (country == null) {
            throw new RuntimeException("Country not set in context");
        }

        DataSource ds = dataSourceManager.getDataSourceForCountry(country);
        Connection conn = ds.getConnection();
        conn.createStatement().execute("USE " + schema);
        return conn;
    }


    @Override
    public void releaseConnection(Object o, Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnknownUnwrapTypeException(unwrapType);
    }

}

package com.accesshr.emsbackend.Entity;

public enum CountryServerConfig {

    UK("talent-flow-server-uk.mysql.database.azure.com", "mtl", "mtl@123456"),
    INDIA("talent-flow-server-db-server.mysql.database.azure.com", "mtl", "mtl@123456");

    private final String serverUrl;
    private final String dbUsername;
    private final String dbPassword;

    CountryServerConfig(String serverUrl, String dbUsername, String dbPassword) {
        this.serverUrl = serverUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    @Override
    public String toString() {
        return "CountryServerConfig{" +
                "serverUrl='" + serverUrl + '\'' +
                ", dbUsername='" + dbUsername + '\'' +
                ", dbPassword='" + dbPassword + '\'' +
                '}';
    }
}

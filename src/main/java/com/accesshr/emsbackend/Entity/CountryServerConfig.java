package com.accesshr.emsbackend.Entity;

public enum CountryServerConfig {

    UK("talent-flow-server-uk.mysql.database.azure.com"),
    INDIA("talent-flow-server-db-server.mysql.database.azure.com");

    private final String serverUrl;

    CountryServerConfig(String serverUrl) {
        this.serverUrl = serverUrl;
    }


    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String toString() {
        return "CountryServerConfig{" +
                "serverUrl='" + serverUrl + '\'' +
                '}';
    }
}

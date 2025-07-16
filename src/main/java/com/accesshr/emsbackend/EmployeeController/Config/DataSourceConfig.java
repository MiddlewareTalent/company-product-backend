package com.accesshr.emsbackend.EmployeeController.Config;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${azure.keyvault.db-username-secret}")
    private String dbUsernameSecretName;

    @Value("${azure.keyvault.db-password-secret}")
    private String dbPasswordSecretName;

    private String dbUsername;
    private String dbPassword;


    @PostConstruct
    public void fetchSecretsFromKeyVault() {
        SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl("https://talentflowkeyvault.vault.azure.net/")
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        dbUsername = secretClient.getSecret(dbUsernameSecretName).getValue();
        dbPassword = secretClient.getSecret(dbPasswordSecretName).getValue();

        System.out.println("Secret value: " + secretClient.getSecret("db-username-mtl").getValue());
        System.out.println("Data Source Config: "+dbUsername);
        System.out.println("Data Source Config: "+dbPassword);
    }

    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(dbUrl);
        ds.setUsername(dbUsername);
        ds.setPassword(dbPassword);
        return ds;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }
}

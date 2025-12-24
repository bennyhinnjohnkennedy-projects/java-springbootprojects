package it.sky.keplero.config;

import com.fasterxml.jackson.databind.JsonNode;
import it.sky.keplero.aws.SecretManagerUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DBConfigurations {
    private final SecretManagerUtils secretManagerUtils;

    public DBConfigurations(SecretManagerUtils secretManagerUtils){
        this.secretManagerUtils = secretManagerUtils;
    }

    @Primary
    @Bean
    public DataSource loadDatabaseCreds(){
        JsonNode secretJson = secretManagerUtils.getSecret();
        String dbUrlSecrets = secretJson.get("dbUrl").asText();
        String dbUserNameSecrets = secretJson.get("dbUsername").asText();
        String dbPasswordSecrets = secretJson.get("dbPassword").asText();

        // Create DataSource using the values from Secrets Manager
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(dbUrlSecrets);
        dataSource.setUsername(dbUserNameSecrets);
        dataSource.setPassword(dbPasswordSecrets);

        return dataSource;
    }
}

package com.chat.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mongodb")
public class MongoDbConfig {
    private String host = "localhost";
    private int port = 27017;
    private String database = "chat_db";
    private String username = "";
    private String password = "";

    public String getUri() {
        if (username != null && !username.isEmpty()) {
            return String.format("mongodb://%s:%s@%s:%d/%s", username, password, host, port, database);
        }
        return String.format("mongodb://%s:%d/%s", host, port, database);
    }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
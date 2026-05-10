package com.chat.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "database")
public class DatabaseConfig {
    private String host = "localhost";
    private int port = 3306;
    private String name = "chat_db";
    private String username = "root";
    private String password = "";

    public String getJdbcUrl() {
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC", host, port, name);
    }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
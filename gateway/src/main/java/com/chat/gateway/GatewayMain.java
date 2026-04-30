package com.chat.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class GatewayMain {

    private static final Logger logger = LoggerFactory.getLogger(GatewayMain.class);

    public static void main(String[] args) {
        String instanceId = args.length > 0 ? args[0] : UUID.randomUUID().toString().substring(0, 8);
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;

        logger.info("Gateway starting on port {}...", port);
        System.out.println("Gateway starting on port " + port + "...");

        GatewayServer server = new GatewayServer(port, instanceId);

        try {
            server.start();
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("GatewayMain interrupted");
        } catch (Exception e) {
            logger.error("Failed to start GatewayServer", e);
            System.err.println("Failed to start GatewayServer: " + e.getMessage());
            System.exit(1);
        }
    }
}

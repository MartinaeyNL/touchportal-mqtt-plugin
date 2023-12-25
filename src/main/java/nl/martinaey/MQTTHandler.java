package nl.martinaey;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MQTTHandler {

    protected final Logger logger;

    protected Map<String, MQTTClient> mqttClients;

    public MQTTHandler(Logger logger) {
        logger.log(Level.INFO, "Registering MQTT Handler...");
        this.logger = logger;
        this.mqttClients = new HashMap<>();
    }

    public void createClient(String id, String host, String port, boolean useSsl) {
        this.createClient(id, host, port, useSsl, false);
    }
    public void createClient(String id, String host, String port, boolean useSsl, boolean useVersion3) {
        this.logger.log(Level.INFO, "Creating MQTT Client '" + id + "'");
        this.mqttClients.put(id, new MQTTClient(id, host, port, useSsl, useVersion3));
    }

    public void disconnectAll() {
        this.logger.log(Level.INFO, "Disconnecting all MQTT clients...");
        this.mqttClients.forEach((key, client) -> client.disconnect());
    }

    public CompletableFuture<?> connectClient(String clientId, String username, String password) {
        this.logger.log(Level.INFO, "Connecting to MQTT client '" + clientId + "'...");
        MQTTClient client = this.getMqttClientById(clientId);
        return client.connect(username, password).whenComplete((sub, throwable) -> {
            if(throwable != null) {
                this.logger.log(Level.SEVERE, "Couldn't connect to '" + clientId + "'!");
                this.logger.log(Level.SEVERE, throwable.getMessage());
            } else {
                this.logger.log(Level.INFO, "Successfully connected to MQTT client '" + clientId + "'!");
            }
        });
    }

    public CompletableFuture<?> subscribeToTopic(String clientId, String topic) {
        MQTTClient client = this.getMqttClientById(clientId);
        return client.subscribeTo(topic, this::onSubscribeMessage).whenComplete((sub, throwable) -> {
            if(throwable != null) {
                this.logger.log(Level.SEVERE, "Couldn't subscribe to topic '" + topic + "' for client '" + clientId + "'!");
                this.logger.log(Level.SEVERE, throwable.getMessage());
            } else {
                this.logger.log(Level.INFO, "Successfully subscribed to topic '" + topic + "' for client '" + clientId + "'!");
            }
        });
    }

    protected MQTTClient getMqttClientById(String clientId) {
        MQTTClient client = this.mqttClients.get(clientId);
        if(client == null) {
            this.logger.log(Level.SEVERE, "Can't connect to MQTT client. This client has not been registered.");
            return null;
        };
        return client;
    }

    protected void onSubscribeMessage(String payload) {
        this.logger.log(Level.INFO, payload);
    }


}

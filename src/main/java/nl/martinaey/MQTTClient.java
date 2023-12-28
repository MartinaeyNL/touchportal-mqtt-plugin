package nl.martinaey;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientBuilder;
import com.hivemq.client.mqtt.MqttVersion;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MQTTClient {

    protected final MqttClient client;

    public MQTTClient(String id, String host, String port, boolean useSsl, boolean useVersion3) {
        MqttClientBuilder builder = MqttClient.builder()
                .identifier(id)
                .serverHost(host)
                .serverPort(Integer.parseInt(port))
                .automaticReconnectWithDefaultConfig();

        if(useSsl) {
            builder = builder.sslWithDefaultConfig();
        }
        if(useVersion3) {
            this.client = builder.useMqttVersion3().buildAsync();
        } else {
            this.client = builder.useMqttVersion5().buildAsync();
        }
    }

    public MQTTClient(MqttClient client) {
        this.client = client;
    }

    /* -------------------------------------------- */

    public CompletableFuture<?> connect(String username, String password) {
        if(this.isVersion5()) {
            return ((Mqtt5AsyncClient) this.client).connectWith()
                    .simpleAuth()
                        .username(username)
                        .password(password.getBytes())
                        .applySimpleAuth()
                    .send();
        } else {
            return ((Mqtt3AsyncClient) this.client).connectWith()
                    .simpleAuth()
                        .username(username)
                        .password(password.getBytes())
                        .applySimpleAuth()
                    .send();
        }
    }

    public void disconnect() {
        if(this.isVersion5()) {
            ((Mqtt5AsyncClient) this.client).disconnect();
        } else {
            ((Mqtt3AsyncClient) this.client).disconnect();
        }
    }

    public CompletableFuture<?> subscribeTo(String topic, Consumer<String>... callbacks) {
        if(this.isVersion5()) {
            return ((Mqtt5AsyncClient) this.client).subscribeWith()
                    .topicFilter(topic)
                    .callback(mqtt5Publish -> {
                        for(Consumer<String> callback : callbacks) {
                            callback.accept(new String(mqtt5Publish.getPayloadAsBytes()));
                        }
                    })
                    .send();
        } else {
            return ((Mqtt3AsyncClient) this.client).subscribeWith()
                    .topicFilter(topic)
                    .callback(mqtt3Publish -> {
                        for(Consumer<String> callback : callbacks) {
                            callback.accept(new String(mqtt3Publish.getPayloadAsBytes()));
                        }
                    })
                    .send();
        }
    }

    protected boolean isVersion5() {
        return this.client.getConfig().getMqttVersion() == MqttVersion.MQTT_5_0;
    }

}

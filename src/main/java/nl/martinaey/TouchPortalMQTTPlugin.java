package nl.martinaey;

import com.christophecvb.touchportal.TouchPortalPlugin;
import com.christophecvb.touchportal.annotations.*;
import com.christophecvb.touchportal.helpers.PluginHelper;
import com.christophecvb.touchportal.model.*;
import com.google.gson.JsonObject;

import java.util.logging.Level;
import java.util.logging.Logger;

@Plugin(version = 1, colorDark = "#203060", colorLight = "#4070F0", name = "TouchPortal MQTT Plugin")
public class TouchPortalMQTTPlugin extends TouchPortalPlugin implements TouchPortalPlugin.TouchPortalPluginListener {

    protected final static Logger LOGGER     = Logger.getLogger(TouchPortalPlugin.class.getName());
    protected static MQTTHandler mqttHandler;
    protected static TouchPortalMQTTPlugin plugin;

    @Setting(name = "MQTT Client Id", defaultValue = "mqtt-client-id")
    protected String clientId;

    @Setting(name = "MQTT Hostname", defaultValue = "localhost")
    protected String host;

    @Setting(name = "MQTT Port", defaultValue = "1883")
    protected String port;

    @Setting(name = "Use SSL / MQTTS", defaultValue = "false")
    protected String useSsl;

    @Setting(name = "Use older MQTT V3.1.1 instead of V5", defaultValue = "false")
    protected String mqttVersion3;

    @Setting(name = "Mqtt Username", defaultValue = "")
    protected String username;

    @Setting(name = "Mqtt Password / Secret", isPassword = true, defaultValue = "")
    protected String password;



    /* ---------------------------------------------------- */

    // Constructor
    protected TouchPortalMQTTPlugin() {
        super(true);
    }

    public static void main(String[] args) {

        LOGGER.log(Level.INFO, "Starting TouchPortalMQTTPlugin...");

        if (args != null && args.length == 1) {
            if (PluginHelper.COMMAND_START.equals(args[0])) {

                // Initialize your Plugin
                plugin = new TouchPortalMQTTPlugin();

                // Initiate the connection with the Touch Portal Plugin System (will trigger an onInfo message with a confirmation from TouchPortal and the initial settings)
                boolean connectedPairedAndListening = plugin.connectThenPairAndListen(plugin);
                if(connectedPairedAndListening) {
                    LOGGER.log(Level.INFO, "TouchPortalMQTTPlugin is now up and running!");
                }
            }
        }
    }

    protected void initialize() {
        LOGGER.log(Level.INFO, "Initializing MQTT Plugin...");

        plugin.sendCreateState("BaseCategory", "testState1", "State for testing purposes", "TestValue");

        // Register MqttClient
        mqttHandler = new MQTTHandler(LOGGER);
        mqttHandler.createClient(this.clientId, this.host, this.port, Boolean.parseBoolean(this.useSsl), Boolean.parseBoolean(this.mqttVersion3));


        mqttHandler.connectClient(this.clientId, this.username, this.password)
                .whenComplete((mqtt5SubAck, throwable) -> {
                    if(throwable == null) {
                        mqttHandler.subscribeToTopic("mqtt-client-id", "master/mqtt-client-id/attribute/+/#");
                    }
                });
    }

    protected void disconnect() {
        mqttHandler.disconnectAll();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        plugin.disconnect();
    }



    /* ------------------------------------------------------------------ */

    @State(defaultValue = "1", categoryId = "BaseCategory")
    @Event(name = "Listen to MQTT topic", format = "When MQTT topic $val broadcasts a new value")
    private String mqttTopicListener;

    @Action(description = "Long Description of Dummy Action with Data Text", format = "Set text to {$text$}", categoryId = "BaseCategory")
    private void actionWithText(@Data String text) {
        LOGGER.log(Level.INFO, "Action actionWithText received: " + text);
    }

    @Action(description = "Another long description", format = "Set text to {$text$}", categoryId = "BaseCategory")
    private void actionWithTextThree(@Data String text) {
        LOGGER.log(Level.INFO, "Action actionWithTextTwo received: " + text);
    }

    private enum Categories {
        /**
         * Category definition example
         */
        @Category(name = "TouchPortal MQTT Plugin", imagePath = "images/icon-24.png")
        BaseCategory
    }

    /* -------------------------------------------- */


    /**
     * Called when the Socket connection is lost or the plugin has received the close Message
     */
    @Override
    public void onDisconnected(Exception exception) {
        LOGGER.log(Level.SEVERE, exception.getMessage());
    }

    /**
     * Called when receiving a message from the Touch Portal Plugin System
     */
    @Override
    public void onReceived(JsonObject jsonMessage) {
        LOGGER.log(Level.INFO, jsonMessage.getAsString());
    }

    /**
     * Called when the Info Message is received when Touch Portal confirms our initial connection is successful
     */
    @Override
    public void onInfo(TPInfoMessage tpInfoMessage) {
        LOGGER.log(Level.INFO, "onInfo() event triggered");

        if(this.verifySettings()) {
            this.initialize();
        }
    }

    /**
     * Called when a List Change Message is received
     */
    @Override
    public void onListChanged(TPListChangeMessage tpListChangeMessage) {
        LOGGER.log(Level.INFO, tpListChangeMessage.toString());
    }

    /**
     * Called when a Broadcast Message is received
     */
    @Override
    public void onBroadcast(TPBroadcastMessage tpBroadcastMessage) {
        LOGGER.log(Level.INFO, tpBroadcastMessage.toString());
    }

    /**
     * Called when a Settings Message is received
     */
    @Override
    public void onSettings(TPSettingsMessage tpSettingsMessage) {
        LOGGER.log(Level.INFO, "onSettings() event triggered");

        this.disconnect();

        if(this.verifySettings()) {
            this.initialize();
        }
    }

    @Override
    public void onNotificationOptionClicked(TPNotificationOptionClickedMessage tpNotificationOptionClickedMessage) {
        LOGGER.log(Level.INFO, "onNotificationOptionClicked() event triggered.");
    }

    protected boolean verifySettings() {

        boolean clientId = this.clientId != null && !this.clientId.isEmpty();
        if(!clientId) LOGGER.log(Level.SEVERE, "Client ID is not set up.");

        boolean host = this.host != null && !this.host.isEmpty();
        if(!host) LOGGER.log(Level.SEVERE, "MQTT Host is not set up.");

        boolean port = this.port != null && !this.port.isEmpty();
        if(!port) LOGGER.log(Level.SEVERE, "MQTT Port is not set up.");

        boolean username = this.username != null && !this.username.isEmpty();
        if(!username) LOGGER.log(Level.SEVERE, "MQTT Username is not set up.");

        boolean password = this.password != null && !this.password.isEmpty();
        if(!password) LOGGER.log(Level.SEVERE, "MQTT Password is not set up");

        boolean useSsl = this.useSsl != null && !this.useSsl.isEmpty() && (this.useSsl.equals("true") || this.useSsl.equals("false"));
        if(!useSsl) LOGGER.log(Level.SEVERE, "MQTT Use SSL is not set up. Should be 'true' or 'false'");

        boolean mqttVersion3 = this.mqttVersion3 != null && !this.mqttVersion3.isEmpty() && (this.mqttVersion3.equals("true") || this.mqttVersion3.equals("false"));
        if(!mqttVersion3) LOGGER.log(Level.SEVERE, "MQTT Version is not set up correctly. Should be 'true' or 'false'");

        return clientId && host && port && username && password && useSsl && mqttVersion3;
    }
}

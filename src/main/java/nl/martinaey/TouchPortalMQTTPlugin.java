package nl.martinaey;

import com.christophecvb.touchportal.TouchPortalPlugin;
import com.christophecvb.touchportal.annotations.*;
import com.christophecvb.touchportal.helpers.PluginHelper;
import com.christophecvb.touchportal.model.*;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Plugin(version = BuildConfig.VERSION_CODE, colorDark = "#203060", colorLight = "#4070F0", name = "TouchPortal MQTT Plugin")
public class TouchPortalMQTTPlugin extends TouchPortalPlugin implements TouchPortalPlugin.TouchPortalPluginListener {

    protected static final Logger LOGGER = Logger.getLogger(TouchPortalPlugin.class.getName());
    protected static MQTTHandler mqttHandler;
    protected static TouchPortalMQTTPlugin plugin;

    protected enum Categories {
        @Category(name = "TouchPortal MQTT Plugin", imagePath = "images/icon-24.png")
        BaseCategory
    }

    @State(defaultValue = "Topic 1", categoryId = "BaseCategory", desc = "Last broadcasted topic")
    @Event(valueChoices = {"Any topic", "Topic 1", "Topic 2", "Topic 3", "Topic 4"}, format = "When MQTT broadcasts a value on $val")
    protected String lastBroadcastedTopic;

    @State(id = "topic1payload", defaultValue = "{}", categoryId = "BaseCategory", desc = "MQTT Topic 1 payload")
    protected String topic1payload;

    @State(id = "topic2payload", defaultValue = "{}", categoryId = "BaseCategory", desc = "MQTT Topic 2 payload")
    protected String topic2payload;

    @State(id = "topic3payload", defaultValue = "{}", categoryId = "BaseCategory", desc = "MQTT Topic 3 payload")
    protected String topic3payload;

    @State(id = "topic4payload", defaultValue = "{}", categoryId = "BaseCategory", desc = "MQTT Topic 4 payload")
    protected String topic4payload;


    /* ------------------------------------------ */
    /*          Plugin / MQTT settings            */
    /* ------------------------------------------ */
    //<editor-fold desc="Plugin / MQTT settings">

    @Setting(name = "MQTT Client Id", defaultValue = "mqtt-client-id")
    protected String clientId;

    @Setting(name = "MQTT Hostname", defaultValue = "localhost")
    protected String host;

    @Setting(name = "MQTT Port", defaultValue = "1883")
    protected String port;

    @Setting(name = "Use SSL / MQTTS", defaultValue = "false")
    protected String useSsl;

    @Setting(name = "Use older MQTT V3 instead of V5", defaultValue = "false")
    protected String mqttVersion3;

    @Setting(name = "Mqtt Username", defaultValue = "")
    protected String username;

    @Setting(name = "Mqtt Password / Secret", isPassword = true, defaultValue = "")
    protected String password;

    //</editor-fold>

    /* ------------------------------------------ */
    /*             MQTT topic options             */
    /* ------------------------------------------ */
    //<editor-fold desc="MQTT topic options">

    // TODO: Replace this with unlimited amount of MQTT topics supported once Java SDK updates.

    protected Map<Integer, String> activeTopics = new HashMap<Integer, String>();

    @Setting(name = "Mqtt Topic #1", defaultValue = "")
    protected String mqttTopic1;

    @Setting(name = "Mqtt Topic #2", defaultValue = "")
    protected String mqttTopic2;

    @Setting(name = "Mqtt Topic #3", defaultValue = "")
    protected String mqttTopic3;

    @Setting(name = "Mqtt Topic #4", defaultValue = "")
    protected String mqttTopic4;

    //</editor-fold>


    /* --------------------------------------------------------------------- */
    /*             Plugin initialization & Java startup function             */
    /* --------------------------------------------------------------------- */
    //<editor-fold desc="Plugin initialization">

    // Constructor
    protected TouchPortalMQTTPlugin() {
        super(true);
    }

    public static void main(String[] args) {
        if (args != null && args.length == 1) {
            if (PluginHelper.COMMAND_START.equals(args[0])) {

                // Initialize your Plugin
                plugin = new TouchPortalMQTTPlugin();

                // Initiate the connection with the Touch Portal Plugin System.
                // (will trigger an onInfo message with a confirmation from TouchPortal and the initial settings)
                boolean connectedPairedAndListening = plugin.connectThenPairAndListen(plugin);
                if(connectedPairedAndListening) {
                    LOGGER.log(Level.INFO, "TouchPortalMQTTPlugin is now up and running!");
                }
            }
        }
    }

    protected void initialize() {
        LOGGER.log(Level.INFO, "Initializing MQTT Plugin...");

        // Register MqttClient
        mqttHandler = new MQTTHandler(LOGGER);
        mqttHandler.createClient(this.clientId, this.host, this.port, Boolean.parseBoolean(this.useSsl), Boolean.parseBoolean(this.mqttVersion3));

        // Connect and subscribe to topics
        mqttHandler.connectClient(this.clientId, this.username, this.password)
                .whenComplete((mqtt5SubAck, throwable) -> {
                    if(throwable == null) {

                        // TODO: Replace this with unlimited amount of MQTT topics supported once Java SDK updates.
                        this.tryToSubscribeToTopic(1, mqttTopic1);

                        mqttHandler.getValueUpdateObserver().subscribe(
                                (updateMessage) -> {
                                    this.updateValueByTopic(updateMessage.topic, updateMessage.value);
                                }, (error) -> {
                                    LOGGER.log(Level.SEVERE, error.getMessage());
                                }
                        );
                    }
                });
    }

    protected void disconnect() {
        // empty
    }

    //</editor-fold>


    /* ------------------------------------------------------- */
    /*             TouchPortal callback functions              */
    /* ------------------------------------------------------- */
    //<editor-fold desc="TP callback Functions">

    // Called when the Socket connection is lost or the plugin has received the close Message
    @Override
    public void onDisconnected(Exception exception) {
        LOGGER.log(Level.SEVERE, "onDisconnected() event triggered");
        plugin.disconnect();
        System.exit(0);
    }

    // Called when receiving a message from the Touch Portal Plugin System
    @Override
    public void onReceived(JsonObject jsonMessage) {
        // unused
    }

    // Called when the Info Message is received when Touch Portal confirms our initial connection is successful
    @Override
    public void onInfo(TPInfoMessage tpInfoMessage) {
        LOGGER.log(Level.INFO, "onInfo() event triggered");

        if(this.verifySettings() && this.verifyMqttTopics()) {
            this.initialize();
        }
    }

    // Called when a List Change Message is received
    @Override
    public void onListChanged(TPListChangeMessage tpListChangeMessage) {
        // unused
    }

    // Called when a Broadcast Message is received
    @Override
    public void onBroadcast(TPBroadcastMessage tpBroadcastMessage) {
        // unused
    }

    // Called when a Settings Message is received
    @Override
    public void onSettings(TPSettingsMessage tpSettingsMessage) {
        LOGGER.log(Level.INFO, "onSettings() event triggered");

        this.disconnect();

        if(this.verifySettings() && this.verifyMqttTopics()) {
            this.initialize();
        }
    }

    @Override
    public void onNotificationOptionClicked(TPNotificationOptionClickedMessage tpNotificationOptionClickedMessage) {
        // unused
    }

    //</editor-fold>


    /* -------------------------------------------------------------- */
    /*             Verify TouchPortal settings functions              */
    /* -------------------------------------------------------------- */
    //<editor-fold desc="Verify settings functions">

    protected boolean verifySettings() {

        boolean hasClientId = this.clientId != null && !this.clientId.isEmpty();
        if(!hasClientId) {
            LOGGER.log(Level.SEVERE, "Client ID is not set up.");
        }

        boolean hasHost = this.host != null && !this.host.isEmpty();
        if(!hasHost) {
            LOGGER.log(Level.SEVERE, "MQTT Host is not set up.");
        }

        boolean hasPort = this.port != null && !this.port.isEmpty();
        if(!hasPort) {
            LOGGER.log(Level.SEVERE, "MQTT Port is not set up.");
        }

        boolean hasUsername = this.username != null && !this.username.isEmpty();
        if(!hasUsername) {
            LOGGER.log(Level.SEVERE, "MQTT Username is not set up.");
        }

        boolean hasPassword = this.password != null && !this.password.isEmpty();
        if(!hasPassword) {
            LOGGER.log(Level.SEVERE, "MQTT Password is not set up");
        }

        boolean hasUseSsl = this.useSsl != null && !this.useSsl.isEmpty() && (this.useSsl.equals("true") || this.useSsl.equals("false"));
        if(!hasUseSsl) {
            LOGGER.log(Level.SEVERE, "MQTT Use SSL is not set up. Should be 'true' or 'false'");
        }

        boolean hasMqttVersion3 = this.mqttVersion3 != null && !this.mqttVersion3.isEmpty() && (this.mqttVersion3.equals("true") || this.mqttVersion3.equals("false"));
        if(!hasMqttVersion3) {
            LOGGER.log(Level.SEVERE, "MQTT Version is not set up correctly. Should be 'true' or 'false'");
        }

        return hasClientId && hasHost && hasPort && hasUsername && hasPassword && hasUseSsl && hasMqttVersion3;
    }


    // Checks whether at least one MQTT topic is inserted in the settings
    protected boolean verifyMqttTopics() {

        // TODO: Replace this with unlimited amount of MQTT topics supported once Java SDK updates.
        boolean topic1 = this.mqttTopic1 != null && !this.mqttTopic1.isEmpty();
        boolean topic2 = this.mqttTopic2 != null && !this.mqttTopic2.isEmpty();
        boolean topic3 = this.mqttTopic3 != null && !this.mqttTopic3.isEmpty();
        boolean topic4 = this.mqttTopic4 != null && !this.mqttTopic4.isEmpty();

        boolean atLeastOnceTopicValid = topic1 || topic2 || topic3 || topic4;
        if(!atLeastOnceTopicValid) {
            LOGGER.log(Level.SEVERE, "No MQTT topics set up.");
        }
        return atLeastOnceTopicValid;

    }

    //</editor-fold>


    /* -------------------------------------------------------------------------- */
    /*             Plugin specific functions such as updating states              */
    /* -------------------------------------------------------------------------- */

    protected void tryToSubscribeToTopic(int index, String mqttTopic) {
        if(mqttTopic != null && !mqttTopic.isEmpty()) {
            this.activeTopics.put(index, mqttTopic);
            mqttHandler.subscribeToTopic("mqtt-client-id", mqttTopic);
        }
    }

    protected void updateValueByTopic(String topic, String value) {

        List<Integer> topics = this.activeTopics.entrySet().stream()
                .filter(entry -> entry.getValue().equals(topic))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if(!topics.isEmpty()) {

            int topicID = topics.get(0);
            this.sendStateUpdate(TouchPortalMQTTPluginConstants.BaseCategory.States.LastBroadcastedTopic.ID, ("Topic " + topicID));
            this.sendStateUpdate((TouchPortalMQTTPluginConstants.BaseCategory.ID + ".state.topic" + topicID + "payload"), value);

            try {
                Thread.sleep(10); // improve this
                this.sendStateUpdate(TouchPortalMQTTPluginConstants.BaseCategory.States.LastBroadcastedTopic.ID, "Any topic");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
            }

        } else {
            LOGGER.log(Level.SEVERE, "Could not trigger event/state update, topic '{0}' does not exist!", topic);
            LOGGER.log(Level.SEVERE, this.activeTopics.toString());
        }
    }
}

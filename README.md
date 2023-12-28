# TouchPortal MQTT Plugin

Hi all!

I was looking for a TouchPortal plugin to communicate with a 3rd party service using MQTT.<br />
There was only a closed source version available, and it didn't include all features I needed for integrating it. So here we are!

:warning: **Very work in progress plugin.** Do use with extra care. :warning:

<br />

## How to Install
WIP

<br />

## TouchPortal Actions / Events
WIP

<br />

## Settings

| Setting                         | Description                                                                                                                 | Default          |
|---------------------------------|-----------------------------------------------------------------------------------------------------------------------------|------------------|
| MQTT Client ID                  | Unique identifier used for the MQTT connection.                                                                             | `mqtt-client-id` |
| MQTT Host                       | Host of the MQTT Broker we should connect to. Use without `mqtt:` prefix.                                                   | `localhost`      |
| MQTT Port                       | Port of the MQTT Broker we should use. Normally `1883` or `8883`.                                                           | `1883`           |
| Use SSL / MQTTS                 | Whether we should connect to the MQTT Broker using a secure connection.<br />Should be set to `false` or `true`            | `false`          |
| Use older MQTT V3 instead of V5 | Some brokers only support connections using MQTT Version 3.1.1,<br />normally not necessary. Should be set to `false` or `true`. | `false`          |
| MQTT Username                   | Username to use for authenticating to the MQTT Broker                                                                       |                  |
| MQTT Password                   | Password to use for authenticating to the MQTT Broker                                                                       |                  |
| MQTT Topic `<number>`           | The MQTT topic we should listen for. Once it broadcasts a message,<br />we trigger an TouchPortal event if set up.               |                  |

<br />

## Known issues

- Launching TP when MQTT broker couldn't connect does not reconnect afterwards.  

<br />

## FAQ

- **"It is not connecting to my MQTT broker!"**<br />
  You can check the Logs tab in TouchPortal to see what exactly is going on. Remember that this is an MQTT client, not a broker, so you will need a MQTT API/broker to connect to.

- **"Can you use multiple MQTT clients?"**<br />
  No. Only 1 client is supported at the moment.

- **"Why is there a maximum of 8 MQTT topics?"**<br />
  This is because of current limitations in the Java SDK of TouchPortal I use. It is being worked on.

<br />
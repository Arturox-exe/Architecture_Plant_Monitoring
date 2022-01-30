package dte.masteriot.mdp.AppleFarmFinal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;

public class Commands extends AppCompatActivity {

    final String serverUri = "tcp://broker.hivemq.com:1883";
    final String subscriptionTopic = "applefarm/gateway1/command";
    final String publishTopic = "applefarm/gateway1/command";
    final String publishMessageBuzzerOn = "Buzzer-On-15";
    final String publishMessageBuzzerOff = "Buzzer-Off-15";
    final String publishMessagePumpOn = "Pump-On-15";
    final String publishMessagePumpOff = "Pump-Off-15-Yes";


    MqttAndroidClient mqttAndroidClient;
    String clientId = "Commands";
    private Button btn_BuzOn,btn_BuzOff,btn_PumpOn,btn_PumpOff;
    String What_Btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commands);
        btn_BuzOn = findViewById(R.id.btn_Buzzer_On);
        btn_BuzOff = findViewById(R.id.btn_Buzzer_Off);
        btn_PumpOn = findViewById(R.id.btn_Pump_On);
        btn_PumpOff = findViewById(R.id.btn_Pump_Off);


        btn_BuzOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                What_Btn = "BuzOn";
                publishMessage();
            }
        });
        btn_BuzOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                What_Btn = "BuzOff";
                publishMessage();
            }
        });
        btn_PumpOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                What_Btn = "PumpOn";
                publishMessage();
            }
        });
        btn_PumpOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                What_Btn = "PumpOff";
                publishMessage();
            }
        });

        clientId = clientId + System.currentTimeMillis();

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    //addToHistory("Reconnected to : " + serverURI);
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic();
                } else {
                    //addToHistory("Connected to: " + serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                //addToHistory("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws JSONException {
                //addToHistory("Incoming message: " + new String(message.getPayload()));
                String message_payload;
                //Toast.makeText(Tree15.this,"Incoming message:" + new String(message.getPayload()),Toast.LENGTH_LONG).show();

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        //mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setCleanSession(true);

        //addToHistory("Connecting to " + serverUri + "...");
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //addToHistory("Failed to connect to: " + serverUri +
                    //        ". Cause: " + ((exception.getCause() == null)?
                    //        exception.toString() : exception.getCause()));
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            //addToHistory(e.toString());
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    public void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //addToHistory("Subscribed to: " + subscriptionTopic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //addToHistory("Failed to subscribe");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
            //addToHistory(e.toString());
        }

    }

    public void publishMessage() {
        MqttMessage message = new MqttMessage();
        switch (What_Btn){
            case "BuzOn": {     message.setPayload(publishMessageBuzzerOn.getBytes());
                message.setRetained(false);
                message.setQos(0);
                break;}
            case "BuzOff": {    message.setPayload(publishMessageBuzzerOff.getBytes());
                message.setRetained(false);
                message.setQos(0);
                break;}
            case "PumpOn": {    message.setPayload(publishMessagePumpOn.getBytes());
                message.setRetained(false);
                message.setQos(0);
                break;}
            case "PumpOff": {   message.setPayload(publishMessagePumpOff.getBytes());
                message.setRetained(false);
                message.setQos(0);
                break;}

        }

        try {
            mqttAndroidClient.publish(publishTopic, message);
            //addToHistory("Message Published");
        } catch (Exception e) {
            e.printStackTrace();
            //addToHistory(e.toString());
        }
        if (!mqttAndroidClient.isConnected()) {
            //addToHistory("Client not connected!");
        }
    }
}
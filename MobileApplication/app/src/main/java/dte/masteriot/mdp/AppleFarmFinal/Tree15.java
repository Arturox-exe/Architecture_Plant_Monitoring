package dte.masteriot.mdp.AppleFarmFinal;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

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
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class Tree15 extends AppCompatActivity {

    final String serverUri = "tcp://broker.hivemq.com:1883";
    final String publishTopic = "applefarm/gateway1/command";
    final String subscriptionTopic = "applefarm/gateway1/tree15";
    String publishMessageLcdOnSendMess;
    final String publishMessageLcdOff = "LCD-Off-15";
    private TextView TV_ph;
    ImageView img_alarm_X, img_alarm_ph;
    String ph_value, x_value;
    MqttAndroidClient mqttAndroidClient;
    String clientId = "Tree15";
    private Button btn_LcdOnSendMess, btn_LcdOff, btn_ok_ph, btn_LcdOnSendMess_bt, btn_LcdOff_bt;
    String What_Btn_tree15;
    EditText text_lcd, et_min_ph, et_max_ph, text_lcd_bt;
    private static final String TAG = "Tree15";
    float finalvalueMinPH;
    float finalvalueMaxPH;
    final static UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String valueMinPH, valueMaxPH;
    OutputStream outputStream = null;
    BluetoothSocket btSocket = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree15);

        btn_LcdOff = findViewById(R.id.btn_LCD_Off);
        btn_LcdOff_bt = findViewById(R.id.btn_LCD_Off_bt);
        text_lcd = findViewById(R.id.edt_sendmesage_lcd);
        text_lcd_bt = findViewById(R.id.edt_sendmesage_lcd_bt);
        TV_ph = findViewById(R.id.textView_PHValue);
        img_alarm_X = findViewById(R.id.powerCircleTF);
        img_alarm_ph = findViewById(R.id.powerCirclePH);
        btn_LcdOnSendMess = findViewById(R.id.btn_LCD_SendMessage);
        btn_LcdOnSendMess_bt = findViewById(R.id.btn_LCD_SendMessage_bt);
        btn_ok_ph = findViewById(R.id.btn_ph_threshold_ok);
        et_min_ph = findViewById(R.id.et_PHMin);
        et_max_ph = findViewById(R.id.et_PHMax);

        //Toast.makeText(Tree15.this, "VALUE MAX PH: " + finalvalueMaxPH, Toast.LENGTH_SHORT).show();
        //Toast.makeText(Tree15.this, "VALUE MIN PH: " + finalvalueMinPH, Toast.LENGTH_SHORT).show();

        //the message for the lcd in the tree15, the formant for the command is LCD-On-Example Message-15
        publishMessageLcdOnSendMess = "LCD-On-" + text_lcd.getText() + "-15";

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println(btAdapter.getBondedDevices());

        BluetoothDevice hc05 = btAdapter.getRemoteDevice("E4:5F:01:3C:92:EB");
        System.out.println(hc05.getName());

        if (!btAdapter.isEnabled()) {
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;
            Toast.makeText(context, "Bluetooth is not enabled, you can't send messages in local", duration).show();
            btn_LcdOff_bt.setEnabled(false);
            btn_LcdOnSendMess_bt.setEnabled(false);
        }

        else {
            int contador = 0;
            do {
                try {
                    btSocket = hc05.createRfcommSocketToServiceRecord(mUUID);
                    System.out.println(btSocket);
                    btSocket.connect();
                    System.out.println(btSocket.isConnected());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                contador++;
            } while (!btSocket.isConnected() && contador < 2);

            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            if (!btSocket.isConnected()) {
                Toast.makeText(context, "Unable to connect via Bluetooth", duration).show();
                btn_LcdOff_bt.setEnabled(false);
                btn_LcdOnSendMess_bt.setEnabled(false);
            } else {
                Toast.makeText(context, "Connected via Bluetooth", duration).show();
            }


            try {
                outputStream = btSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Action for the button LCD SEND MESSAGE
        btn_LcdOnSendMess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //the message for the lcd in the tree15, the formant for the command is LCD-On-Example Message-15
                publishMessageLcdOnSendMess = "LCD-On-" + text_lcd.getText() + "-15";
                What_Btn_tree15 = "LcdSendMessage";
                publishMessage();
            }
        });
        //Action for the button OFF
        btn_LcdOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                What_Btn_tree15 = "LcdOff";
                publishMessage();
            }
        });

        //Action for the button LCD SEND MESSAGE
        btn_LcdOnSendMess_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //the message for the lcd in the tree15, the formant for the command is LCD-On-Example Message-15
                try {
                    String LCDMessage = "LCD-On-" + text_lcd_bt.getText() + "-15";
                    byte[] bytes = LCDMessage.getBytes(Charset.defaultCharset());
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //Action for the button OFF
        btn_LcdOff_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    byte[] bytes = publishMessageLcdOff.getBytes(Charset.defaultCharset());
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_ok_ph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                valueMinPH = et_min_ph.getText().toString();
                valueMaxPH = et_max_ph.getText().toString();

                if (TextUtils.isEmpty(valueMinPH) || TextUtils.isEmpty(valueMaxPH)) {
                    Toast.makeText(Tree15.this, "Please complete both values", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    //POST JSON PH
                    JSONObject jsonObjectPH = new JSONObject();
                    try {
                        finalvalueMinPH = Float.parseFloat(valueMinPH);
                        jsonObjectPH.put("minPH", finalvalueMinPH);
                        finalvalueMaxPH = Float.parseFloat(valueMaxPH);
                        jsonObjectPH.put("maxPH", finalvalueMaxPH);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    AndroidNetworking.initialize(getApplicationContext());

                    //0Mp6fB08nsr27y7ORz3g : deviceToken from the Device (Tree15) in Thingsboard
                    AndroidNetworking.post("https://srv-iot.diatel.upm.es/api/v1/0Mp6fB08nsr27y7ORz3g/telemetry")
                            .addJSONObjectBody(jsonObjectPH) // posting json
                            .addHeaders("Content-Type", "application/json")
                            .setTag("ph")
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    // do anything with response
                                }

                                @Override
                                public void onError(ANError error) {
                                    // handle error
                                    if (error.getErrorCode() != 0) {
                                        // received error from server
                                        Log.d(TAG, "onError errorCode : " + error.getErrorCode());
                                        Log.d(TAG, "onError errorBody : " + error.getErrorBody());
                                        Log.d(TAG, "onError errorDetail : " + error.getErrorDetail());

                                    } else {
                                        // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                                        Log.d(TAG, "onError errorCode : " + error.getErrorCode());
                                        Log.d(TAG, "onError errorBody : " + error.getErrorBody());
                                        Log.d(TAG, "onError errorDetail : " + error.getErrorDetail());
                                    }
                                }
                            });
                    Toast.makeText(Tree15.this, "OK", Toast.LENGTH_SHORT).show();

                }

            }
        });
        //MQTT
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
                message_payload = new String(message.getPayload());
                JSONObject jsonObject = new JSONObject(message_payload);
                ph_value = jsonObject.getString("ph");
                TV_ph.setText(ph_value);
                x_value = jsonObject.getString("x");

                //Toast.makeText(Tree15.this, "RECEIVE MESS VALUE MAX ON CLICK PH: " + finalvalueMaxPH, Toast.LENGTH_SHORT).show();
                //Toast.makeText(Tree15.this, "RECEIVE MESS VALUE MIN ON CLICK PH: " + finalvalueMinPH, Toast.LENGTH_SHORT).show();

                //Set the first threshold values to PH
                if (finalvalueMaxPH == 0 && finalvalueMinPH == 0) {
                    finalvalueMaxPH = 7.0F;
                    finalvalueMinPH = 6.5F;
                }
                //Toast.makeText(Tree15.this, "PH: "+finalvalueMinPH+"//"+finalvalueMaxPH, Toast.LENGTH_SHORT).show();
                if ((Float.parseFloat(ph_value) > finalvalueMinPH) && (Float.parseFloat(ph_value) < finalvalueMaxPH)) {
                    img_alarm_ph.setBackgroundResource(R.drawable.round_button_on);
                } else {
                    img_alarm_ph.setBackgroundResource(R.drawable.round_button_off);
                }

                if (Float.parseFloat(x_value) < -0.9) {
                    //Toast.makeText(Tree15.this,"TREE FALL TREE 15!",Toast.LENGTH_SHORT).show();
                    img_alarm_X.setBackgroundResource(R.drawable.round_button_off);
                } else {
                    img_alarm_X.setBackgroundResource(R.drawable.round_button_on);
                }

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
        switch (What_Btn_tree15) {
            case "LcdSendMessage": {
                message.setPayload(publishMessageLcdOnSendMess.getBytes());
                message.setRetained(false);
                message.setQos(0);
                break;
            }
            case "LcdOff": {
                message.setPayload(publishMessageLcdOff.getBytes());
                message.setRetained(false);
                message.setQos(0);
                break;
            }

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

    @Override
    protected void onPause() {
        super.onPause();
        // write to shared preferences
        SharedPreferences.Editor values = getSharedPreferences("My_shared_preference", MODE_PRIVATE).edit();
        values.putFloat("maxPH", finalvalueMaxPH);
        values.putFloat("minPH", finalvalueMinPH);
        values.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // read from shared preferences
        SharedPreferences prefs = getSharedPreferences("My_shared_preference", MODE_PRIVATE);
        finalvalueMaxPH = prefs.getFloat("maxPH", 0);
        finalvalueMinPH = prefs.getFloat("minPH", 0);

    }
}
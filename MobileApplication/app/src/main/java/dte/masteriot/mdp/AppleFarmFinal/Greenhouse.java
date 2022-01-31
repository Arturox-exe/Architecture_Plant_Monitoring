package dte.masteriot.mdp.AppleFarmFinal;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
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
import org.json.JSONObject;
import org.json.JSONException;

public class Greenhouse extends AppCompatActivity {

    final String serverUri = "tcp://broker.hivemq.com:1883";
    final String subscriptionTopic = "applefarm/gateway1";
    final String publishTopic = "applefarm/gateway1";
    MqttAndroidClient mqttAndroidClient;
    String clientId = "Greenhouse";
    private Button btn_tree15,btn_commands,btn_commands_btn;
    private TextView Temp_Value;
    private TextView Hum_Value;
    private TextView Mois_Value2;
    ImageView img_alarm_temp,img_alarm_hum,img_alarm_mois;
    String temp_value,hum_value,mois_value,message_payload;
    private static final String TAG = "THRESHOLDS";
    EditText et_min_temp,et_max_temp,et_min_hum,et_max_hum,et_min_mois,et_max_mois;
    Button btn_ok_temp,btn_ok_hum,btn_ok_mois;
    float finalvalueMinTemp,finalvalueMaxTemp,finalvalueMinHum,finalvalueMaxHum,finalvalueMinMois,finalvalueMaxMois;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greenhouse);

        btn_commands = findViewById(R.id.btn_commands);
        btn_commands_btn = findViewById(R.id.btn_commands_bt);
        btn_tree15 = findViewById(R.id.btn_tree15);
        Temp_Value = findViewById(R.id.textView_TempValue);
        Hum_Value = findViewById(R.id.textView_HumValue);
        Mois_Value2 = findViewById(R.id.textView_MoisValue2);
        img_alarm_temp=findViewById(R.id.powerCircleTMP);
        img_alarm_hum=findViewById(R.id.powerCircleHUM);
        img_alarm_mois = findViewById(R.id.powerCircleMOIS2);

        btn_ok_hum = findViewById(R.id.btn_hum_threshold_ok);
        btn_ok_temp = findViewById(R.id.btn_temp_threshold_ok);
        btn_ok_mois = findViewById(R.id.btn_mois_threshold_ok);

        et_min_temp = findViewById(R.id.et_TempMin);
        et_max_temp = findViewById(R.id.et_TempMax);
        et_min_hum = findViewById(R.id.et_HumMin);
        et_max_hum = findViewById(R.id.et_HumMax);
        et_min_mois = findViewById(R.id.et_MoisMin);
        et_max_mois = findViewById(R.id.et_MoisMax);

        btn_ok_temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String valueMinTemp = et_min_temp.getText().toString();
                String valueMaxTemp = et_max_temp.getText().toString();

                if (TextUtils.isEmpty(valueMinTemp) || TextUtils.isEmpty(valueMaxTemp)) {
                    Toast.makeText(Greenhouse.this, "Please complete both values", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    //POST JSON TEMP
                    JSONObject jsonObjectTemp = new JSONObject();
                    try {
                        finalvalueMinTemp = Float.parseFloat(valueMinTemp);
                        jsonObjectTemp.put("minTemp", finalvalueMinTemp);
                        finalvalueMaxTemp = Float.parseFloat(valueMaxTemp);
                        jsonObjectTemp.put("maxTemp", finalvalueMaxTemp);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    AndroidNetworking.initialize(getApplicationContext());

                    //Ld98QFtrViyOI4AOUb37 : deviceToken from the Device in Thingsboard
                    AndroidNetworking.post("https://srv-iot.diatel.upm.es/api/v1/Ld98QFtrViyOI4AOUb37/telemetry")
                            //.setOkHttpClient(okHttpClient)
                            .addJSONObjectBody(jsonObjectTemp) // posting json
                            .addHeaders("Content-Type", "application/json")
                            .setTag("temperature")
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
                    Toast.makeText(Greenhouse.this, "OK", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btn_ok_mois.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String valueMinMois = et_min_mois.getText().toString();
                String valueMaxMois = et_max_mois.getText().toString();

                if (TextUtils.isEmpty(valueMinMois) || TextUtils.isEmpty(valueMaxMois)) {
                    Toast.makeText(Greenhouse.this, "Please complete both values", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    //POST JSON MOIS
                    JSONObject jsonObjectMOIS = new JSONObject();
                    try {
                        finalvalueMinMois = Float.parseFloat(valueMinMois);
                        jsonObjectMOIS.put("minMois", finalvalueMinMois);

                        finalvalueMaxMois = Float.parseFloat(valueMaxMois);
                        jsonObjectMOIS.put("maxMois", finalvalueMaxMois);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //CLIENT REST API

                    AndroidNetworking.initialize(getApplicationContext());

                    //Ld98QFtrViyOI4AOUb37 : deviceToken from the Device in Thingsboard
                    AndroidNetworking.post("https://srv-iot.diatel.upm.es/api/v1/Ld98QFtrViyOI4AOUb37/telemetry")
                            //.setOkHttpClient(okHttpClient)
                            .addJSONObjectBody(jsonObjectMOIS) // posting json
                            .addHeaders("Content-Type", "application/json")
                            .setTag("moisture")
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
                    Toast.makeText(Greenhouse.this, "OK", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_ok_hum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String valueMinHum = et_min_hum.getText().toString();
                String valueMaxHum = et_max_hum.getText().toString();

                if (TextUtils.isEmpty(valueMinHum) || TextUtils.isEmpty(valueMaxHum)) {
                    Toast.makeText(Greenhouse.this, "Please complete both values", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    //POST JSON HUM
                    JSONObject jsonObjectHum = new JSONObject();
                    try {
                        finalvalueMinHum = Float.parseFloat(valueMinHum);
                        jsonObjectHum.put("minHum", finalvalueMinHum);
                        finalvalueMaxHum = Float.parseFloat(valueMaxHum);
                        jsonObjectHum.put("maxHum", finalvalueMaxHum);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //Ld98QFtrViyOI4AOUb37 : deviceToken from the Device in Thingsboard
                    AndroidNetworking.post("https://srv-iot.diatel.upm.es/api/v1/Ld98QFtrViyOI4AOUb37/telemetry")
                            .addJSONObjectBody(jsonObjectHum) // posting json
                            .setTag("humidity")
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    // do anything with response
                                    //tv_test.setText(response.toString());
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
                                        Log.d(TAG, "onError errorDetail : " + error.getErrorDetail());
                                    }
                                }
                            });
                    Toast.makeText(Greenhouse.this, "OK", Toast.LENGTH_SHORT).show();

                }
            }
        });
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        //Tasks for the buttons
        btn_commands.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent go_commands = new Intent(Greenhouse.this,Commands.class);
                startActivity(go_commands);
            }
        });

        btn_commands_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!btAdapter.isEnabled()) {
                    Context context = getApplicationContext();
                    int duration = Toast.LENGTH_SHORT;
                    Toast.makeText(context, "Please enable bluetooth", duration).show();
                }
                else {
                    Intent go_commands_bt = new Intent(Greenhouse.this, Commands_bt.class);
                    startActivity(go_commands_bt);
                }
            }
        });

        btn_tree15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent go_tree15 = new Intent(Greenhouse.this,Tree15.class);
                startActivity(go_tree15);
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
            public void messageArrived(String topic, MqttMessage message){
                //addToHistory("Incoming message: " + new String(message.getPayload()));
                //Toast.makeText(Greenhouse.this,"Incoming message:" + new String(message.getPayload()),Toast.LENGTH_LONG).show();
                message_payload =  new String(message.getPayload());
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(message_payload);
                    temp_value = jsonObject.getString("temperature");
                    Temp_Value.setText(temp_value);
                    hum_value = jsonObject.getString("humidity");
                    Hum_Value.setText(hum_value);
                    mois_value = jsonObject.getString("moisture");
                    Mois_Value2.setText(mois_value);
                    //Set the first Case
                    if (finalvalueMaxTemp == 0 && finalvalueMinTemp == 0){
                        finalvalueMaxTemp = 24F;
                        finalvalueMinTemp = 18F;
                    }
                    //Toast.makeText(Greenhouse.this, "TEMP: "+finalvalueMinTemp+"//"+finalvalueMaxTemp, Toast.LENGTH_SHORT).show();
                    if ((Float.parseFloat(temp_value)<finalvalueMaxTemp) && (Float.parseFloat(temp_value)>finalvalueMinTemp)){
                        //Toast.makeText(Tree15.this,"TREE FALL TREE 15!",Toast.LENGTH_SHORT).show();
                        img_alarm_temp.setBackgroundResource(R.drawable.round_button_on);
                        //Toast.makeText(Greenhouse.this, "NO ALARM", Toast.LENGTH_SHORT).show();
                    } else {
                        img_alarm_temp.setBackgroundResource(R.drawable.round_button_off);
                        //Toast.makeText(Greenhouse.this, "ALARM", Toast.LENGTH_SHORT).show();
                    }

                    //Set the first Case
                    if (finalvalueMaxHum == 0 && finalvalueMinHum == 0){
                        finalvalueMaxHum = 80F;
                        finalvalueMinHum = 60F;
                    }
                    //Toast.makeText(Greenhouse.this, "HUM: "+finalvalueMinHum+"//"+finalvalueMaxHum, Toast.LENGTH_SHORT).show();
                    if ((Float.parseFloat(hum_value)<finalvalueMaxHum) && (Float.parseFloat(hum_value)>finalvalueMinHum)){
                        //Toast.makeText(Tree15.this,"TREE FALL TREE 15!",Toast.LENGTH_SHORT).show();
                        img_alarm_hum.setBackgroundResource(R.drawable.round_button_on);
                    } else {
                        img_alarm_hum.setBackgroundResource(R.drawable.round_button_off);
                    }

                    //Set the first Case
                    if (finalvalueMinMois == 0 && finalvalueMaxMois == 0){
                        finalvalueMaxMois = 90F;
                        finalvalueMinMois = 60F;
                    }
                    //Toast.makeText(Greenhouse.this, "MOIS: "+finalvalueMinMois+"//"+finalvalueMaxMois, Toast.LENGTH_SHORT).show();
                    if ((Float.parseFloat(mois_value)<finalvalueMaxMois) && (Float.parseFloat(mois_value)>finalvalueMinMois)){
                        //Toast.makeText(Tree15.this,"TREE FALL TREE 15!",Toast.LENGTH_SHORT).show();
                        img_alarm_mois.setBackgroundResource(R.drawable.round_button_on);
                    } else {
                        img_alarm_mois.setBackgroundResource(R.drawable.round_button_off);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
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

    @Override
    protected void onPause() {
        super.onPause();
        // write to shared preferences
        SharedPreferences.Editor values = getSharedPreferences("My_shared_preference2", MODE_PRIVATE).edit();
        values.putFloat("maxTemp",finalvalueMaxTemp );
        values.putFloat("minTemp",finalvalueMinTemp );
        values.putFloat("maxHum",finalvalueMaxHum );
        values.putFloat("minHum",finalvalueMinHum );
        values.putFloat("maxMois",finalvalueMaxMois );
        values.putFloat("minMois",finalvalueMinMois );
        values.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // read from shared preferences
        SharedPreferences prefs = getSharedPreferences("My_shared_preference2",MODE_PRIVATE);
        finalvalueMaxTemp = prefs.getFloat("maxTemp",0);
        finalvalueMinTemp = prefs.getFloat("minTemp",0);
        finalvalueMaxHum = prefs.getFloat("maxHum",0);
        finalvalueMinHum = prefs.getFloat("minHum",0);
        finalvalueMaxMois = prefs.getFloat("maxMois",0);
        finalvalueMinMois = prefs.getFloat("minMois",0);

    }
}
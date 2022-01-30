package dte.masteriot.mdp.AppleFarmFinal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Handler;

public class Commands_bt extends AppCompatActivity {

    final static UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final String MessageBuzzerOn = "Buzzer-On-15";
    final String MessageBuzzerOff = "Buzzer-Off-15";
    final String MessagePumpOn = "Pump-On-15";
    final String MessagePumpOff = "Pump-Off-15-Yes";
    OutputStream outputStream = null;

    private Button btn_BuzOn,btn_BuzOff,btn_PumpOn,btn_PumpOff;

    public Commands_bt() {
    }

    @Override
    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.activity_commands_bt);
        btn_BuzOn = findViewById(R.id.btn_Buzzer_On);
        btn_BuzOff = findViewById(R.id.btn_Buzzer_Off);
        btn_PumpOn = findViewById(R.id.btn_Pump_On);
        btn_PumpOff = findViewById(R.id.btn_Pump_Off);

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println(btAdapter.getBondedDevices());

        BluetoothDevice hc05 = btAdapter.getRemoteDevice("E4:5F:01:3C:92:EB");
        System.out.println(hc05.getName());

        BluetoothSocket btSocket = null;
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
        } while (!btSocket.isConnected() && contador < 3);

        try {
            outputStream = btSocket.getOutputStream();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        btn_BuzOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    byte[] bytes = MessageBuzzerOn.getBytes(Charset.defaultCharset());
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_BuzOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    byte[] bytes = MessageBuzzerOff.getBytes(Charset.defaultCharset());
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_PumpOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    byte[] bytes = MessagePumpOn.getBytes(Charset.defaultCharset());
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_PumpOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    byte[] bytes = MessagePumpOff.getBytes(Charset.defaultCharset());
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });



/*

        try {
            OutputStream outputStream = btSocket.getOutputStream();
            outputStream.write(48);
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream inputStream = null;
        try {
            inputStream = btSocket.getInputStream();
            inputStream.skip(inputStream.available());

            for (int i = 0; i < 26; i++) {

                byte b = (byte) inputStream.read();
                System.out.println((char) b);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            btSocket.close();
            System.out.println(btSocket.isConnected());
        } catch (IOException e) {
            e.printStackTrace();
        }
*/

    }
}
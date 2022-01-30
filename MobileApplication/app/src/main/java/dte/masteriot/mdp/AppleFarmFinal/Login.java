package dte.masteriot.mdp.AppleFarmFinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Login extends AppCompatActivity {

    private Button btn_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_submit = findViewById(R.id.submit_btn);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent go_greenhouse = new Intent(Login.this,Greenhouse.class);
                startActivity(go_greenhouse);
            }
        });
    }
}
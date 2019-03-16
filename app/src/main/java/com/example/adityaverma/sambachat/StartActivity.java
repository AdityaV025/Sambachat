package com.example.adityaverma.sambachat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    //Setting Up The SignUp And LogIn Button.
    private Button signupbtn;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //Finding Each Button With Their Unique Id's.
        signupbtn = (Button) findViewById(R.id.start_reg_btn);
        loginBtn = (Button) findViewById(R.id.log_in_Btn);

        //Setting Up The EventListener On The SignUp Button.
        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //After Clicking on the SignUp Button The User Will Move To RegisterActivity.
                Intent regIntent = new Intent(StartActivity.this , RegisterActivity.class);
                startActivity(regIntent);

            }
        });

        //Setting Up The EventListener On The LogInButton Button.
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //After Clicking on the LogIn Button The User Will Move To LoginActivity.
                Intent loginIntent = new Intent(StartActivity.this , LoginActivity.class);
                startActivity(loginIntent);

            }
        });


    }
}

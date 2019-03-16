package com.example.adityaverma.sambachat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    //Setting Up The EdiText Or Instance Variables.
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private Button mLoginBtn;

    //Setting The Toolbar.
    private Toolbar mToolBar;

    //Setting Up The FireBase Authentication.
    private FirebaseAuth mAuth;

    //Setting the Progress Dialog.
    private ProgressDialog mLogInProgress;

    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Getting the instance of the authentication in the current Activity.
        mAuth = FirebaseAuth.getInstance();

        //Setting the ToolBar With It's Unique Id.
        mToolBar = (Toolbar) findViewById(R.id.login_appBar);

        //Setting the ActionBar With The ToolBar.
        setSupportActionBar(mToolBar);

        //Setting The Title Of the Login Activity in The AppBar.
        getSupportActionBar().setTitle("Log In");

        //Setting The BackButton In The LoginActivity.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Finding The EdiText With It's Unique Id.
        mEmail = (TextInputEditText) findViewById(R.id.login_email);
        mPassword = (TextInputEditText) findViewById(R.id.login_password);
        mLoginBtn = (Button) findViewById(R.id.log_in_Btn);

        //Setting the LoginProgress Dialog.
        mLogInProgress = new ProgressDialog(LoginActivity.this);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        //Setting The EventListener On Login Button.
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Getting the Text From the EditText and Converting them In String Variables.
                String email = mEmail.getEditableText().toString();
                String password = mPassword.getEditableText().toString();

                //Checking If The email is password is empty or not.
                //If NOT empty then we will set the following properties.
                if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){

                    //Setting the LoginProgress Title.
                    mLogInProgress.setTitle("Logging In");

                    //Setting the Message Of the ProgressDialog/
                    mLogInProgress.setMessage("Please Wait While We Login You");

                    //Disabling the foreign touch by the user.
                    mLogInProgress.setCanceledOnTouchOutside(false);

                    //Showing the ProgressDialog.
                    mLogInProgress.show();

                    //Calling the LoginUser Method with email and password as a parameter.
                    loginUser(email , password);

                }

            }
        });

    }

    //Setting up a new Private Method to Login The User In The App.
    private void loginUser(final String email, String password) {

        //Method to Sign In The User With Email And Password.
        mAuth.signInWithEmailAndPassword(email , password).addOnCompleteListener
                (this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                //If The task(Authentication) is Successful Then we will get The Current User and
                //dismiss the LogInProgress Dialog.
                if (task.isSuccessful()) {

                    String user_current_id = mAuth.getCurrentUser().getUid();
                    String deviceToken  = FirebaseInstanceId.getInstance().getToken();

                    mUsersDatabase.child(user_current_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mLogInProgress.dismiss();
                            //Setting up an Intent to move the user to MainActivity If the Logged In Successfully.
                            Intent mainIntent = new Intent(LoginActivity.this , MainActivity.class);
                            //Setting the flags in the Intent So the User Can't go back to StartActivity After Logging In.
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                        }
                    });

                }else {

                    //If the task(Authentication) is NOT Successful then we will display a Toast Message.
                    mLogInProgress.hide();

                    Toast.makeText(LoginActivity.this , task.getException().toString() , Toast.LENGTH_LONG).show();

                }

            }
        });

    }
}

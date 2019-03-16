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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //Setting Up The EdiText Or Instance Variables.
    private TextInputEditText mName;
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;
    private Button mCreateBtn;

    //Setting The Toolbar.
    private Toolbar mToolBar;

    //Setting Up The FireBase Authentication.
    private FirebaseAuth mAuth;

    private DatabaseReference mDataBase;

    //Setting the Progress Dialog.
    private ProgressDialog mRegProgess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Getting the instance of the authentication in the current Activity.
        mAuth = FirebaseAuth.getInstance();

        //Setting the RegisterProgress Dialog.
        mRegProgess = new ProgressDialog(this);

        //Getting the Text From the EditText and Converting them In String Variables.
        mName = (TextInputEditText) findViewById(R.id.reg_name);
        mEmail = (TextInputEditText) findViewById(R.id.reg_email);
        mPassword = (TextInputEditText) findViewById(R.id.reg_password);
        mCreateBtn = (Button) findViewById(R.id.create_btn);

        //Setting the ToolBar With It's Unique Id.
        mToolBar = (Toolbar) findViewById(R.id.register_appBar);

        //Setting the ActionBar With The ToolBar.
        setSupportActionBar(mToolBar);

        //Setting The Title Of the Register Activity in The AppBar.
        getSupportActionBar().setTitle("Create Account");

        //Setting The BackButton In The LoginActivity.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Setting The EventListener On Create User Button.
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Getting the Text From the EditText and Converting them In String Variables.
                String name = mName.getEditableText().toString();
                String email = mEmail.getEditableText().toString();
                String password = mPassword.getEditableText().toString();

                //Checking If The email is password is empty or not.
                //If NOT empty then we will set the following properties.
                if (!TextUtils.isEmpty(name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){

                    //Setting the LoginProgress Title.
                    mRegProgess.setTitle("Registering User");

                    //Setting the Message Of the ProgressDialog
                    mRegProgess.setMessage("Please Wait While We Create Your New Account!");

                    //Disabling the foreign touch by the user.
                    mRegProgess.setCanceledOnTouchOutside(false);

                    //Showing the ProgressDialog.
                    mRegProgess.show();

                    //Calling the registerUser Method with Name, email and password as a parameter.
                    registerUser(name , email , password);

                }else
                    {
                        //If The Registering is Not Successful then We Will Display a Toast Message.
                        Toast.makeText(RegisterActivity.this , "You Have An Empty Field" , Toast.LENGTH_LONG).show();
                    }

            }
        });

    }

    //Creating a private Method to RegisterUser With Input Parameters in Name, email and password.
    private void registerUser(final String name, String email, String password) {

        //Creating the User With Email and Password
        mAuth.createUserWithEmailAndPassword(email , password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                //If the task(Registering is Successful) then we will do the following.
               if (task.isSuccessful()){

                   String deviceToken = FirebaseInstanceId.getInstance().getToken();

                           FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();

                           if (current_user != null){

                               String uid = current_user.getUid();

                               mDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                               HashMap<String , String> userMap = new HashMap<>();
                               userMap.put("device_token" , deviceToken);
                               userMap.put("name" , name);
                               userMap.put("status" , "Hi There I am Using SambaChat");
                               userMap.put("user_image" , "default");
                               userMap.put("thumb_image" , "default");

                               mDataBase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {

                                       if (task.isSuccessful()){

                                           //Dismissing the RegProgress Dialog.
                                           mRegProgess.dismiss();

                                           //Setting up an Intent so the user can move to MainActivity after Registering themselves.
                                           Intent mainIntent = new Intent(RegisterActivity.this , MainActivity.class);

                                           //Adding Flags So the User Can't go back to StartActivity After Registering.
                                           mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                           startActivity(mainIntent);
                                           finish();

                                       }

                                   }
                               });

                           }

               }else
               {

                   //If the task is NOT Successful Then We Will Hide The Progress Dialog and Display a Toast.
                   mRegProgess.hide();
                   Toast.makeText(RegisterActivity.this , task.getException().toString() , Toast.LENGTH_LONG).show();
               }

            }
        });


    }
}

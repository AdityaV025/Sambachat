package com.example.adityaverma.sambachat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class StatusActivity extends AppCompatActivity {

    //Setting the Instance of the toolbar.
    private Toolbar mStatusToolBar;

    //Setting the EditText of Status & Button.
    private TextInputEditText mStatusEditText;
    private Button mSaveBtn;

    //FireBase Reference of the StatusDatabase;
    private DatabaseReference mStatusDatabase;

    //FireBaseUser of the CurrentUser.
    private FirebaseUser mCurrentUser;

    //Progress Dialog
    ProgressDialog mStatusProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Getting the Instance of the CurrentUser From the FirebaseAuth.
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Getting the UID and setting up in a String.
        String current_uid = mCurrentUser.getUid();

        //Pointing the database to the USERS --> UID of the User.
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        //Setting up the ToolBar From it's unique Id.
        mStatusToolBar = (Toolbar) findViewById(R.id.status_appBarLayout);

        //Getting the previous status and storing it in a string to display it in the StatusActivity.
        String status_value = getIntent().getStringExtra("status_value");

        //Setting up the Actual ToolBar.
        setSupportActionBar(mStatusToolBar);

        //Setting the Title of the ToolBar.
        Objects.requireNonNull(getSupportActionBar()).setTitle("Change Status");

        //Getting the ActionBar and Setting the Back Button.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Finding the widgets With their Unique ID's.
        mStatusEditText = (TextInputEditText) findViewById(R.id.status_input);
        mSaveBtn = (Button) findViewById(R.id.save_btn);

        //Setting the EdiText With the Previous Status.
        mStatusEditText.setText(status_value);

        //Setting up and OnClickListener On Save Button.
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Making the Progress Dialog Object.
                mStatusProgressDialog = new ProgressDialog(StatusActivity.this);

                //Setting the Title Of the Progress Dialog.
                mStatusProgressDialog.setTitle("Saving Status");

                //Setting the Message of the Progress Dialog.
                mStatusProgressDialog.setMessage("We Are Saving Your Status");

                //Disabling the foreign touch by the user during the progress dialog.
                mStatusProgressDialog.setCanceledOnTouchOutside(false);

                //Showing the Progress Dialog.
                mStatusProgressDialog.show();

                //Getting the Status EditText and saving it in a String.
                String status = mStatusEditText.getEditableText().toString();

                //Setting up an OnCompleteListener if the task is Successful.
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            //If the task is Successful we will dismiss the progressDialog.
                            mStatusProgressDialog.dismiss();

                        }else {

                            //If the task is not Successful then we will hide the ProgressDialog and display a Toast Message.
                            mStatusProgressDialog.hide();
                            Toast.makeText(StatusActivity.this , "You Have Some Error" , Toast.LENGTH_LONG).show();

                        }

                    }
                });

            }
        });

    }
}

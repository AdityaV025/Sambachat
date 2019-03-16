package com.example.adityaverma.sambachat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    //Instance Variables or Widgets.
    private ImageView mDisplayImage;
    private TextView mDisplayName;
    private TextView mStatus;
    private TextView mDisplayTotFriends;
    private Button mSndReqBtn;
    private Button mDeclineBtn;

    //Reference of the Database.
    private DatabaseReference mUsersDatabase;

    //Getting the reference of the CurrentUser.
    private FirebaseUser mCurrentUser;

    private String mFriendshipState = "not_friends";

    private DatabaseReference mFriendReqDatabase;

    private DatabaseReference mFriendDatabase;

    private DatabaseReference mNotificationsDatabase;

    private DatabaseReference mRootRef;

    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Getting the User_id of the Current User who is using the App.
        final String user_id = getIntent().getStringExtra("user_id");

        //Setting the FireBaseDatabase Reference to display the Information of the User in the Profile Activity.
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mUsersDatabase.keepSynced(true);

        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        mFriendReqDatabase.keepSynced(true);

        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendDatabase.keepSynced(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

        mNotificationsDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mNotificationsDatabase.keepSynced(true);

        //Getting the Auth of the CurrentUser.
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mUserRef.keepSynced(true);

        //Finding the Widgets of the Profile_Activity.
        mDisplayImage = (ImageView) findViewById(R.id.profile_displa_image);
        mDisplayName = (TextView) findViewById(R.id.profile_name);
        mStatus = (TextView) findViewById(R.id.profile_status);
        mDisplayTotFriends = (TextView) findViewById(R.id.profile_friends);
        mSndReqBtn = (Button) findViewById(R.id.send_frReq_btn);
        mDeclineBtn = (Button) findViewById(R.id.declineBtn);

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        //Setting up the Progress Dialog.
        final ProgressDialog mProgressDialog = new ProgressDialog(this);

        //Setting the Progress Dialog and Setting the Title & Message.
        mProgressDialog.setTitle("Fetching User Profile");
        mProgressDialog.setMessage("Please Wait While We Fetch the Desired User Info.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        //Adding an mUsersDatabase and adding an ValueEventListener and setting up an onDataChange.
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Setting the display_name and getting the value of the name in the FireBase Database.
                String display_name = dataSnapshot.child("name").getValue().toString();
                String display_status = dataSnapshot.child("status").getValue().toString();
                String display_userImage = dataSnapshot.child("user_image").getValue().toString();

                //Setting up the Data in the Profile Activity.
                mDisplayName.setText(display_name);
                mStatus.setText(display_status);

                //Getting the image as an URL and downloading it into the Profile Activity.
                Picasso.get().load(display_userImage).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar_img).into(mDisplayImage);

                mFriendReqDatabase.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) {

                            String req_type = dataSnapshot.child(user_id).child("friend_status")
                                    .getValue().toString();

                            if (req_type.equals("received")) {

                                mFriendshipState = "req_received";
                                mSndReqBtn.setText("Accept Friend Request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            } else if (req_type.equals("sent")) {

                                mFriendshipState = "req_sent";
                                mSndReqBtn.setText("Cancel Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }

                            mProgressDialog.dismiss();

                        } else {

                            mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)) {

                                        mFriendshipState = "friends";
                                        mSndReqBtn.setText("Unfriend This Person");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSndReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSndReqBtn.setEnabled(false);

                if (mFriendshipState.equals("not_friends")) {

                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_Requests/" + mCurrentUser.getUid() + "/" + user_id + "/friend_status", "sent");
                    requestMap.put("Friend_Requests/" + user_id + "/" + mCurrentUser.getUid() + "/friend_status", "received");
                    requestMap.put("Notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Toast.makeText(ProfileActivity.this, "Error", Toast.LENGTH_LONG).show();

                            } else {

                                mFriendshipState = "req_sent";
                                mSndReqBtn.setText("Cancel Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }

                            mSndReqBtn.setEnabled(true);

                        }
                    });


                }
                if (mFriendshipState.equals("req_sent")) {

                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid())
                                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mSndReqBtn.setEnabled(true);
                                        mFriendshipState = "not_friends";
                                        mSndReqBtn.setText("Send Friend Request");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    }
                                });

                            }

                        }
                    });

                }

                if (mFriendshipState.equals("req_received")) {

                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date", currentDate);


                    friendsMap.put("Friend_Requests/" + mCurrentUser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_Requests/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError == null) {

                                mSndReqBtn.setEnabled(true);
                                mFriendshipState = "friends";
                                mSndReqBtn.setText("Unfriend this Person");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }

                        }
                    });

                }

                if (mFriendshipState.equals("friends")) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {


                            if (databaseError == null) {

                                mFriendshipState = "not_friends";
                                mSndReqBtn.setText("Send Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }

                            mSndReqBtn.setEnabled(true);

                        }
                    });

                }
            }
        });

        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map declineMap = new HashMap();

                declineMap.put("Friend_Requests/" + mCurrentUser.getUid() + "/" + user_id, null);
                declineMap.put("Friend_Requests/" + user_id + "/" + mCurrentUser.getUid(), null);

                mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if (databaseError == null) {

                            mFriendshipState = "not_friends";
                            mSndReqBtn.setText("Send Friend Request");

                        } else {
                            String error = databaseError.getMessage();
                            Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                        }

                        mSndReqBtn.setEnabled(true);
                    }
                });

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mCurrentUser != null){

            mUserRef.child("online").setValue("true");

        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCurrentUser != null){

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }

    }
}
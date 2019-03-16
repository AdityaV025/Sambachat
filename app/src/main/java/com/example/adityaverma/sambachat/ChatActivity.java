package com.example.adityaverma.sambachat;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private Toolbar mChatToolBar;

    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserRef;

    private TextView mTitleView;
    private TextView mLastSeenTextView;
    private CircleImageView mProfileImage;
    private ImageView mOnlineIconView;

    private EditText mChatMessageView;
    private ImageButton mAddChatBtn;
    private ImageButton mSendChatBtn;

    private RecyclerView mMessageList;
    private List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 20;
    private int mCurrentPage = 1;
    private static final int GALLERY_PICK = 1;

    private SwipeRefreshLayout swipeRefreshLayout;

    private int itemPos = 1;

    private String mLastKey = "";
    private String mPrevKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolBar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolBar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

        mChatUser = getIntent().getStringExtra("user_id");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mUserRef.keepSynced(true);

        final String chat_user_name = getIntent().getStringExtra("user_name");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custome_layout, null);

        actionBar.setCustomView(action_bar_view);

        mTitleView = findViewById(R.id.title_name);
        mLastSeenTextView = findViewById(R.id.last_seen_view);
        mProfileImage = findViewById(R.id.custom_profile_image);
        mOnlineIconView = findViewById(R.id.custom_online_icon);

        mAdapter = new MessageAdapter(messagesList);

        mMessageList = (RecyclerView) findViewById(R.id.mMessageList);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        linearLayoutManager = new LinearLayoutManager(this);

        mMessageList.setHasFixedSize(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        linearLayoutManager.setStackFromEnd(true);
        mMessageList.setLayoutManager(linearLayoutManager);

        mMessageList.setAdapter(mAdapter);

        loadMessages();

        mTitleView.setText(chat_user_name);

        mChatMessageView = (EditText) findViewById(R.id.message_chat);
        mAddChatBtn = (ImageButton) findViewById(R.id.add_button_chat);
        mSendChatBtn = (ImageButton) findViewById(R.id.send_chat_button);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                if (online.equals("true")) {

                    mLastSeenTextView.setText("Online");
                    mOnlineIconView.setVisibility(View.VISIBLE);

                } else {

                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    Long LastTime = Long.parseLong(online);
                    String LastSeenTime = getTimeAgo.getTimeAgo(LastTime, getApplicationContext());
                    mLastSeenTextView.setText(LastSeenTime);

                    mOnlineIconView.setVisibility(View.INVISIBLE);

                }

                Picasso.get()
                        .load(image)
                        .placeholder(R.drawable.default_avatar_img)
                        .resize(100, 100)
                        .into(mProfileImage);

//                    Glide.with(ChatActivity.this).load(image).into(mProfileImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChatUser)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserApp = new HashMap();
                    chatUserApp.put("Chat/" + mCurrentUser.getUid() + "/" + mChatUser, chatAddMap);
                    chatUserApp.put("Chat/" + mChatUser + "/" + mCurrentUser.getUid(), chatAddMap);

                    mRootRef.updateChildren(chatUserApp, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Log.d("CHAT_LOG", databaseError.getMessage());

                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mAddChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Using an Intent to get all the images from the gallery.
                Intent galleryIntent = new Intent();

                //Setting up the path of the image source.
                galleryIntent.setType("image/*");

                //Getting the content(images) from the above path.
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                //Returning the Activity with a result(selected Image).
                startActivityForResult(Intent.createChooser(galleryIntent , "SELECT IMAGE") , GALLERY_PICK);

            }
        });

        mSendChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();

            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;

                itemPos = 0;

                loadMoreMessage();

            }
        });

    }

    private void loadMoreMessage() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUser.getUid()).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){

                    messagesList.add(itemPos++, message);

                } else {

                    mPrevKey = mLastKey;

                }


                if(itemPos == 1) {

                    mLastKey = messageKey;

                }

                mAdapter.notifyDataSetChanged();

                swipeRefreshLayout.setRefreshing(false);

                linearLayoutManager.scrollToPositionWithOffset(10 , 0);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUser.getUid()).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                itemPos++;

                if (itemPos == 1){

                    mLastKey = dataSnapshot.getKey();
                    mPrevKey = dataSnapshot.getKey();


                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessageList.scrollToPosition(messagesList.size() - 1);

                swipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {

        String message = mChatMessageView.getText().toString();

        if (!TextUtils.isEmpty(message)){

            String current_user_ref = "messages/" + mCurrentUser.getUid() + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUser.getUid();

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUser.getUid()).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("Message" , message);
            messageMap.put("Seen" , false);
            messageMap.put("Type" , "text");
            messageMap.put("Time" , ServerValue.TIMESTAMP);
            messageMap.put("From" , mCurrentUser.getUid());

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id , messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id , messageMap);

            mChatMessageView.setText(" ");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if (databaseError != null) {

                        Log.d("CHAT_LOG", databaseError.getMessage());

                    }

                }
            });

        }

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
package com.example.adityaverma.sambachat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import Model.Users_model;
import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    //Instantiating the ToolBar.
    private Toolbar mUsersToolbar;

    //Instantiating the RecyclerView.
    private RecyclerView mUsersRecylerView;

    //Instantiating the Firebase Database.
    private DatabaseReference mUsersDatabase;

    private FirebaseUser mCurrentUser;

    private DatabaseReference mUserRef;

    private FirebaseAuth mAuth;

    private ImageView mOnlineIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        //Getting the Toolbar by finding the Unique Id.
        mUsersToolbar = (Toolbar) findViewById(R.id.allusers_AppBar);

        //Setting the SupportActionBar with the Toolbar.
        setSupportActionBar(mUsersToolbar);

        //Setting the Title of the ToolBar.
        getSupportActionBar().setTitle("All Users");

        //Setting the Support Action Bar with Back Button.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Getting the Reference of the FirebaseDatabase and pointing it to the Users in the Database.
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        //Finding the recyclerView with it's unique Id.
        mUsersRecylerView = (RecyclerView) findViewById(R.id.all_users_view);

        //Setting the Size if the recyclerView as Fixed.
        mUsersRecylerView.setHasFixedSize(true);

        //Setting the LayoutManager of the RecyclerView as a LinearLayoutManager.
        mUsersRecylerView.setLayoutManager(new LinearLayoutManager(this));

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mAuth = FirebaseAuth.getInstance();

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        mUserRef.keepSynced(true);

        mOnlineIcon = (ImageView) findViewById(R.id.custom_online_icon);

    }

    //Creating an OnStart Method.
    @Override
    protected void onStart() {
        super.onStart();

        if (mCurrentUser != null){

            mUserRef.child("online").setValue("true");
        }

        //Querying the Database with the name id.
        Query query = mUsersDatabase.orderByChild("name").limitToLast(50);

        //Setting the up the FirebaseRecycerOption and passing the Model of the Data and the query of the Database.
        FirebaseRecyclerOptions<Users_model> options = new FirebaseRecyclerOptions.Builder<Users_model>()
                .setQuery(query, Users_model.class).build();

        //Setting the FirebaseRecyclerAdapter and passing the Model Class and View Holder.
        FirebaseRecyclerAdapter recyclerAdapter = new FirebaseRecyclerAdapter<Users_model, UsersViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users_model model) {

                //Setting the View with the correct data.
                holder.setName(model.getName());
                holder.setStatus(model.getStatus());
                holder.setUser_thumbImage(model.getThumb_image());

                //Getting the position of the user clicking on the View.
                final String user_id = getRef(position).getKey();

                //Setting up an OnClickListener on the View.
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Making an Intent to go to the ProfileActivity.
                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);

                        //Setting the user_id of the correct user.
                        profileIntent.putExtra("user_id", user_id);

                        //Starting the Activity.
                        startActivity(profileIntent);

                    }
                });

            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                //Setting up the LayoutInflater and passing on the SingleUserLayout.
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_users_layout, parent, false);

                //Returning the UserViewHolder
                return new UsersViewHolder(view);

            }
        };

        //Setting(Joining) the RecyclerView and Adapter.
        mUsersRecylerView.setAdapter(recyclerAdapter);

        //StartListening for the commands.
        recyclerAdapter.startListening();

    }

    //Setting up an ViewHolder Class which extends with RecylerView.
    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        //Creating a new Method to set the Name in the RecyclerView.
        public void setName(String name) {

            //Finding the TextView if the name with it's unique Id.
            TextView mSingleUserName = mView.findViewById(R.id.user_single_name);
            mSingleUserName.setText(name);

        }

        //Creating a new Method to set the Status in the RecyclerView.
        public void setStatus(String status) {

            //Finding the TextView of the status with it's unique Id.
            TextView mSingleUserStatus = mView.findViewById(R.id.user_single_status);
            mSingleUserStatus.setText(status);

        }

        //Creating a new Method to set the thumb_image in the RecyclerView.
        public void setUser_thumbImage(String thumb_image) {

            //Finding the CircleImageView of the thumb_image with it's unique Id.
            CircleImageView mUserImageView = mView.findViewById(R.id.user_thumb_image);

            //Loading the image with Picasso Library.
            Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar_img).into(mUserImageView);


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

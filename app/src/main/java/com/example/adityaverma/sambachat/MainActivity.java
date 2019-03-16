package com.example.adityaverma.sambachat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import Adapters.SectionsPageAdapter;

public class MainActivity extends AppCompatActivity {

    //Authentication For Firebase.
    private FirebaseAuth mAuth;

    //Toolbar
    private Toolbar mToolBar;

    //Setting up the ViewPager, SectionsAdapter & TabLayout.
    private ViewPager mViewPager;
    private SectionsPageAdapter mSectionsPageAdapter;
    private TabLayout mTabLayout;

    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Getting the Present instance of the FireBase Authentication
        mAuth = FirebaseAuth.getInstance();

        //Finding The Toolbar with it's unique Id.
        mToolBar = (Toolbar) findViewById(R.id.main_page_toolbar);

        //Setting Up the ToolBar in The ActionBar.
        setSupportActionBar(mToolBar);

        //Setting the Title in the AppBarLayout.
        getSupportActionBar().setTitle("Lapit Chat");

        //Finding The ViewPager With It's Unique Id.
        mViewPager = (ViewPager) findViewById(R.id.main_tab_pager);

        //Finding the TabLayout With It's Unique Id.
        mTabLayout = (TabLayout) findViewById(R.id.mainTabs);

        //Getting the Adapter to and instantiating it.
        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        //Joining The Adapter With The ViewPager to populate the Fragments.
        mViewPager.setAdapter(mSectionsPageAdapter);

        //Connecting the ViewPager With The TabLayout.
        mTabLayout.setupWithViewPager(mViewPager);

        if (mAuth.getCurrentUser() != null){

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(mAuth.getCurrentUser().getUid());
            mUserRef.keepSynced(true);

        }
    }

    //OnStart Method is started when the Authentication Starts.
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null).
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null){

            startUser();

        } else {

                mUserRef.child("online").setValue("true");
                Log.d("STARTING THE ACTIVITY" , "TRUE");

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
            Log.d("STOPPING THE ACTIVITY" , "TRUE");

        }

    }

    private void startUser() {

        //Sending the user in the StartActivity If the User Is Not Logged In.
        Intent startIntent = new Intent(MainActivity.this , StartActivity.class);
        startActivity(startIntent);
        //Finishing Up The Intent So the User Can't Go Back To MainActivity Without LoggingIn.
        finish();

    }

    //Setting The Menu Options In The AppBarLayout.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         //Inflating the Menu with the Unique R.menu.Id.
         getMenuInflater().inflate(R.menu.main_menu , menu);

         return true;
    }

    //Setting the Individual Item In The Menu.(Logout Button)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_btn){

            FirebaseAuth.getInstance().signOut();
            startUser();

        }

        //User Account Settings
        else if (item.getItemId() == R.id.user_acc_settings){

            //Starting an Intent to MainActivity =====> SettingsActivity.
            Intent accountSettingsIntent = new Intent(MainActivity.this , SettingsActivity.class);
            startActivity(accountSettingsIntent);

        }

        //All Users
        else if (item.getItemId() == R.id.main_menu_allUsers){

            //Starting an Intent to MainActivity =====> SettingsActivity.
            Intent alluserIntent = new Intent(MainActivity.this , UsersActivity.class);
            startActivity(alluserIntent);

        }

        return true;
    }

}



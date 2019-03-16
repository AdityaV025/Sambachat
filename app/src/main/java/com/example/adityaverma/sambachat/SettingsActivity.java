package com.example.adityaverma.sambachat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    //Firebase Database Instance.
    private DatabaseReference mUserDataBase;

    //Firebase User Instance.
    private FirebaseUser mCurrentUser;

    private StorageReference mImageStorage;

    //Widgets Instance variables.
    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;
    private CircleImageView mThumb_Image;
    private Button mChangeStatus;
    private Button mChangeImgBtn;

    private static final int GALLERY_PICK = 1;

    private ProgressDialog mProgressDialog;

    private String user_id;

    private DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Finding Widgets with their Unique Id's
        mDisplayImage = (CircleImageView) findViewById(R.id.avatar_img);
        mName = (TextView) findViewById(R.id.settings_display_name);
        mStatus = (TextView) findViewById(R.id.settings_display_status);
        mChangeStatus = (Button) findViewById(R.id.settings_changeStatus_Btn);
        mChangeImgBtn = (Button) findViewById(R.id.settings_changeImg_Btn);

        //Getting The Instance of Current User.
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Getting The UID of the Current User.
       String current_uid = mCurrentUser.getUid();

       //Getting the Storage Reference from the FirebaseStorage.
        mImageStorage = FirebaseStorage.getInstance().getReference();

       user_id = FirebaseAuth.getInstance().getUid();

        //Populating the RealTimeDatabase with USERS --> UID.
        mUserDataBase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mUserDataBase.keepSynced(true);

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mUserRef.keepSynced(true);

       //Setting Up An ValueEventListener.
       mUserDataBase.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

               //Getting The Values(Child of USERS) and storing them in a String.
               String name = dataSnapshot.child("name").getValue().toString();
               final String user_image = dataSnapshot.child("user_image").getValue().toString();
               String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
               String status = dataSnapshot.child("status").getValue().toString();

               //Setting up in the UI.
               mName.setText(name);
               mStatus.setText(status);


               if(!user_image.equals("default")){

                   Picasso.get().load(user_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar_img).into(mDisplayImage, new Callback() {
                       @Override
                       public void onSuccess() {

                       }

                       @Override
                       public void onError(Exception e) {

                           Picasso.get().load(user_image).placeholder(R.drawable.default_avatar_img).into(mDisplayImage);

                       }
                   });

               }

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

       //Setting up an OnClickListener on Change Status Button.
       mChangeStatus.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               //Getting the status from the realtime database and storing it in a string.
               String status_display = mStatus.getText().toString();

               //Making an Intent to Move the user to SettingsActivity --> StatusActivity.
               Intent changestatusIntent = new Intent(SettingsActivity.this , StatusActivity.class);
               changestatusIntent.putExtra("status_value" , status_display);
               startActivity(changestatusIntent);

           }
       });

       //Setting up an OnClickListener on an ImageButton.
       mChangeImgBtn.setOnClickListener(new View.OnClickListener() {
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

    }

    //Overriding a Method to get the result.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Checking if the selected image is true or not and the result is Ok or not.
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            //Getting the Data of the Image and saving it in a Uri.
            Uri imageUri = data.getData();

            //Instantiating the cropImage feature and setting the ratio in 1:1 and a window size of 500 x 500px.
            CropImage.activity(imageUri).setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(SettingsActivity.this);

        }

        //Checking if the image is cropped or not.
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            //Checking if the result is Ok or not, if yes we will store the image in a uri.
            if (resultCode == RESULT_OK) {

                //Setting up the Progress dialog.
                mProgressDialog = new ProgressDialog(SettingsActivity.this);

                //Setting the title of the progress dialog.
                mProgressDialog.setTitle("Uploading Image");

                //Setting the Message of the Progress Dialog.
                mProgressDialog.setMessage("Please Wait While We Upload Your Image");

                //Disabling the foreign touch of the user of the Progress Dialog.
                mProgressDialog.setCanceledOnTouchOutside(false);

                //Showing the Progress Dialog.
                mProgressDialog.show();

                //Getting the Uri of the image and saving it in resultUri.
                Uri resultUri = result.getUri();

                //Getting the path of the thumb_image of the User.
                File thumb_image_path = new File(resultUri.getPath());

                //Getting the Current UID of the User and storing it in a String.
                final String uid_img = mCurrentUser.getUid();

                //Saving the thumbimage in a Bitmap.
                Bitmap thumb_imagebmp = null;
                try {

                    //Compressing the thumb_image via a Library.
                    thumb_imagebmp = new Compressor(this)

                            //Setting the MaxHeight of the compressed image.
                            .setMaxHeight(200)

                            //Setting the MaxWidth of the Compressed image.
                            .setMaxWidth(200)

                            //Setting the Quality of the Compressed image.
                            .setQuality(75)

                            //Getting the path of the actual(uncompressed image).
                            .compressToBitmap(thumb_image_path);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Saving the image in the Firebase Storage in the form of the Bytes.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                assert thumb_imagebmp != null;
                thumb_imagebmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                //Saving the image in the Firebase Storage.
                StorageReference filepath = mImageStorage.child("profile_images").child(uid_img + "user_image.jpg");

                //Saving the thumb_filePath of the Thumb_image and getting the path.
                final StorageReference thumb_filePath = mImageStorage.child("profile_images").child("thumbs").child(uid_img + "user_image.jpg");

                //If the resultUri is nor Empty or NULL.

                //We Will setup an OnCompleteListener to store the image in the desired location in the storage.
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                        //If the task is Successful we will display a toast.
                        if (task.isSuccessful()) {

                            //Getting the url of the User_image and adding an EventListener.
                            mImageStorage.child("profile_images").child(uid_img + "user_image.jpg")
                                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    //Converting the downloaded url which is in the uri form and saving it into a String.
                                    final String downloadUrl = uri.toString();

                                    //Uploading the thumb_file path in the form of bytes.
                                    UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);

                                    //Setting up an OnCompleteListener on the uploadTask.
                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                                            //Getting the Url of the thumb_image and saving it into a Uri.
                                            mImageStorage.child("profile_images").child(uid_img + "user_image.jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {

                                                    //Getting the Uri and saving it into a String.
                                                    String thumb_image_url = uri.toString();

                                                    if (task.isSuccessful()) {

                                                        //Getting the user_image's and thumb_image's url and saving it into a Map and with their correct naming in the Database.
                                                        Map update_hashmap = new HashMap();
                                                        update_hashmap.put("user_image", downloadUrl);
                                                        update_hashmap.put("thumb_image", thumb_image_url);

                                                        //Saving the url in the Firebase Database and adding an OnCompleteListener.
                                                        mUserDataBase.updateChildren(update_hashmap).addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task) {

                                                                //Dismissing the Progress Dialog if the task gets successful.
                                                                mProgressDialog.dismiss();

                                                            }
                                                        });

                                                    } else {

                                                        Toast.makeText(SettingsActivity.this, "Error in Uploading The File ", Toast.LENGTH_LONG).show();

                                                    }

                                                }
                                            });
                                        }
                                    });

                                }
                            });


                        } else {

                            Toast.makeText(SettingsActivity.this, "Error", Toast.LENGTH_LONG).show();

                            mProgressDialog.dismiss();

                        }
                    }
                });

                //If the task is not successful then we will display an Error Message.
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();

            }
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

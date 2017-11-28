package com.uottawa.dutyhelper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;


public class ProfileSettingActivity extends AppCompatActivity {

    private static final int GALLERY_INTENT = 2;

    private DatabaseReference mUserData;
    private StorageReference mStorage;
    private String mUserId;

    private CircularImageView mProfileImage;
    private TextView mNameTextView;
    private TextView mEmailTextView;
    private TextView mPoints;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting);

        mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUserData = FirebaseDatabase.getInstance().getReference("users").child(mUserId);
        mStorage = FirebaseStorage.getInstance().getReference();

        mNameTextView = (TextView) findViewById(R.id.user_name);
        mEmailTextView = (TextView) findViewById(R.id.user_email);
        mPoints = (TextView) findViewById((R.id.Points));

        mProfileImage = (CircularImageView) findViewById(R.id.profile_image);
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectPicture = new Intent(Intent.ACTION_PICK);
                selectPicture.setType("image/*");
                startActivityForResult(selectPicture, GALLERY_INTENT);
            }
        });

        loadPicture();

        mUserData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("firstName").getValue(String.class) + " ";
                name += dataSnapshot.child("lastName").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                String points = "Points: "+Integer.toString(
                        dataSnapshot.child("points").getValue(Integer.class));

                mNameTextView.setText(name);
                mEmailTextView.setText(email);
                mPoints.setText(points);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            StorageReference filepath = mStorage.child("ProfilePicture").child(mUserId);
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    loadPicture();
                    Toast.makeText(ProfileSettingActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadPicture() {
        StorageReference filepath = mStorage.child("ProfilePicture").child(mUserId);
        Glide.with(getApplicationContext())
                .using(new FirebaseImageLoader())
                .load(filepath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .signature(new MediaStoreSignature("image/*", System.currentTimeMillis(), 0))
                .error(R.drawable.default_avatar)
                .centerCrop()
                .into(mProfileImage);
    }

}

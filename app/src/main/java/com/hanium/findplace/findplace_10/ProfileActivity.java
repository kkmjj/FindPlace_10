package com.hanium.findplace.findplace_10;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.hanium.findplace.findplace_10.models.UserModel;

public class ProfileActivity extends AppCompatActivity {

    private FrameLayout frameLayout;
    private LinearLayout linearLayout;
    private ImageView profileBackground;
    private ImageView close;
    private ImageView profile;
    private TextView nickName;
    private TextView email;
    private Button requestChat;

    private UserModel profileUser;

    private FirebaseRemoteConfig firebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String splash_background = firebaseRemoteConfig.getString(getString(R.string.rc_background));
        getWindow().setStatusBarColor(Color.parseColor(splash_background));

        Intent intent = getIntent();
        String uid = intent.getStringExtra("uid");

        profile = (ImageView) findViewById(R.id.ProfileActivity_ImageView_Profile);
        nickName = (TextView) findViewById(R.id.ProfileActivity_TextView_nickName);
        email = (TextView) findViewById(R.id.ProfileActivity_TextView_email);
        requestChat = (Button) findViewById(R.id.ProfileActivity_Button_chatRequest);

        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                profileUser = dataSnapshot.getValue(UserModel.class);
                Glide.with(ProfileActivity.this).load(profileUser.getProfileURL()).apply(new RequestOptions().circleCrop()).into(profile);
                nickName.setText(profileUser.getNickName());
                email.setText(profileUser.getEmail());
                requestChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent1 = new Intent(ProfileActivity.this, ChatActivity.class);
                        intent1.putExtra("uid", profileUser.getUid());
                        startActivity(intent1);
                        ProfileActivity.this.finish();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        frameLayout = (FrameLayout) findViewById(R.id.ProfileActivity_FrameLayout);
//        linearLayout = (LinearLayout) findViewById(R.id.ProfileActivity_LinearLayout);
//        linearLayout.setBackgroundColor(Color.parseColor(splash_background));

        profileBackground = (ImageView) findViewById(R.id.ProfileActivity_ImageView_ProfileBackground);
        close = (ImageView) findViewById(R.id.ProfileActivity_ImageView_Close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.this.finish();
            }
        });

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fromlefttocenter, R.anim.fromcentertoright);
    }

}

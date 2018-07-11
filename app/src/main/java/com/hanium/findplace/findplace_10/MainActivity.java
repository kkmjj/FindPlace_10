package com.hanium.findplace.findplace_10;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.hanium.findplace.findplace_10.fragment.ChatListFragment;
import com.hanium.findplace.findplace_10.fragment.PeopleFragment;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private FrameLayout frameLayout;
    private TextView mainMessage;
    private BottomNavigationView bottomNavigationView;

    private FirebaseUser currentUser;
    private String currentUid;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String splash_background = firebaseRemoteConfig.getString(getString(R.string.rc_background));
        getWindow().setStatusBarColor(Color.parseColor(splash_background));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUid = currentUser.getUid();

        frameLayout = (FrameLayout) findViewById(R.id.MainActivity_FrameLayout);


        //mainMessage = (TextView) findViewById(R.id.MainActivity_TextView_centerMessage);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.MainActivity_BottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            //선택되었을때 보여줄 프레그먼트 선택.
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.BottomNavigation_People:
                        
                        getFragmentManager().beginTransaction().replace(R.id.MainActivity_FrameLayout, new PeopleFragment()).commit();


                        return true;

                    case R.id.BottomNavigation_Chat:

                        getFragmentManager().beginTransaction().replace(R.id.MainActivity_FrameLayout, new ChatListFragment()).commit();


                        return true;
                    case R.id.BottomNavigation_MyAccount:


                        return true;
                }

                return false;
            }
        });

    }
}

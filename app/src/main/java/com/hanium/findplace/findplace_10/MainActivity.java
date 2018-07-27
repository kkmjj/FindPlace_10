package com.hanium.findplace.findplace_10;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.hanium.findplace.findplace_10.fragment.ChatListFragment;
import com.hanium.findplace.findplace_10.fragment.MyAccountFragment;
import com.hanium.findplace.findplace_10.fragment.PeopleFragment;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private BottomNavigationView bottomNavigationView;

    private FirebaseUser currentUser;
    private String currentUid;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    PeopleFragment peopleFragment;
    ChatListFragment chatListFragment;
    MyAccountFragment myAccountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String splash_background = firebaseRemoteConfig.getString(getString(R.string.rc_background));
        getWindow().setStatusBarColor(Color.parseColor(splash_background));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUid = currentUser.getUid();

        peopleFragment = new PeopleFragment();
        chatListFragment = new ChatListFragment();
        myAccountFragment = new MyAccountFragment();

        viewPager = (ViewPager) findViewById(R.id.MainActivity_ViewPager);
        viewPager.setAdapter(new MyViewPagerAdapter(this.getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(3);


        bottomNavigationView = (BottomNavigationView) findViewById(R.id.MainActivity_BottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            //선택되었을때 보여줄 프레그먼트 선택.
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.BottomNavigation_People:

                        viewPager.setCurrentItem(0);

                        return true;

                    case R.id.BottomNavigation_Chat:

                        viewPager.setCurrentItem(1);


                        return true;
                    case R.id.BottomNavigation_MyAccount:

                        viewPager.setCurrentItem(2);

                        return true;
                }

                return false;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.BottomNavigation_People);
                        return;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.BottomNavigation_Chat);
                        return;
                    default:
                        bottomNavigationView.setSelectedItemId(R.id.BottomNavigation_MyAccount);
                        return;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setCurrentItem(1);
        bottomNavigationView.setSelectedItemId(R.id.BottomNavigation_Chat);

        makeTokenForPushMessage();

    }

    //뷰페이저를 통한 화면전환.
    private class MyViewPagerAdapter extends FragmentStatePagerAdapter {

        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 0) {
                return peopleFragment;
            } else if (position == 1) {
                return chatListFragment;
            } else {
                return myAccountFragment;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    //로그인 토큰값 저장.
    public void makeTokenForPushMessage(){

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String newToken = FirebaseInstanceId.getInstance().getToken();
        Map<String, Object> map = new HashMap<>();
        map.put("pushToken", newToken);

        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).updateChildren(map);

    }

}

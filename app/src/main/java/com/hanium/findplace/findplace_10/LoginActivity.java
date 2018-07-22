package com.hanium.findplace.findplace_10;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button login;
    private Button signUp;

    private final int LOCATION_ACCESS = 100;
    private boolean permission_check = false;

    private FirebaseRemoteConfig firebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String splash_background = firebaseRemoteConfig.getString(getString(R.string.rc_background)).toString();
        getWindow().setStatusBarColor(Color.parseColor(splash_background));

        email = (EditText) findViewById(R.id.LoginActivity_EditText_email);
        password = (EditText) findViewById(R.id.LoginActivity_EditText_password);
        login = (Button) findViewById(R.id.LoginActivity_Button_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //로그인

                FirebaseAuth.getInstance().signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
                            //로그인 성공
                            permissionCheck();
                            if(permission_check){
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }else{
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(LoginActivity.this, "위치권한허가요망", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            //로그인 실패
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });
        signUp = (Button) findViewById(R.id.LoginActivity_Button_signUp);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                finish();
            }
        });

        //자동로그인.
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            //로그인 성공
            permissionCheck();
            if(permission_check){
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }else{
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(LoginActivity.this, "위치권한허가요망", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void permissionCheck(){

        if(ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_ACCESS);
        }else{
            permission_check = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case LOCATION_ACCESS:
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length >0
                        && grantResults[0]==PackageManager.PERMISSION_GRANTED){

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    permission_check = true;

                }else{

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    permission_check = false;
                }
                return;
            }

    }
}

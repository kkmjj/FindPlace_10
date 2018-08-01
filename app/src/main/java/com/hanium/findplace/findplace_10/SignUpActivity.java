package com.hanium.findplace.findplace_10;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.hanium.findplace.findplace_10.models.UserModel;

public class SignUpActivity extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM = 10;
    private ImageView profile;
    private EditText email;
    private EditText password;
    private EditText nickName;
    private EditText phoneNumber;
    private Button join;
    private Button cancel;
    private Uri profileUri;
    private String profileURL;

    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        //해제할 수도 있음.
        firebaseAuth.signOut();

        String splash_background = firebaseRemoteConfig.getString(getString(R.string.rc_background));
        getWindow().setStatusBarColor(Color.parseColor(splash_background));


        profile = (ImageView) findViewById(R.id.SignUpActivity_ImageView_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);

            }
        });
        email = (EditText) findViewById(R.id.SignUpActivity_EditText_email);
        password = (EditText) findViewById(R.id.SignUpActivity_EditText_password);
        nickName = (EditText) findViewById(R.id.SignUpActivity_EditText_nickName);
        phoneNumber = (EditText) findViewById(R.id.SignUpActivity_EditText_phoneNumber);
        join = (Button) findViewById(R.id.SignUpActivity_Button_join);
        join.setBackgroundColor(Color.parseColor(splash_background));
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //회원가입

                //유효성 검사.
                if(!checkValidator()){
                    return;
                }

                firebaseAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            //db 등록
                            String uid = task.getResult().getUser().getUid();

                            setFirebaseDatabase(uid);
                            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                            Toast.makeText(SignUpActivity.this, "가입완료", Toast.LENGTH_SHORT).show();
                            finish();

                        }else{
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });
        cancel = (Button) findViewById(R.id.SignUpActivity_Button_cancel);
        cancel.setBackgroundColor(Color.parseColor(splash_background));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            }
        });

    }

    public void setFirebaseDatabase(final String uid){
        //db 저장.

        FirebaseStorage.getInstance().getReference().child("profileImages").child(uid).putFile(profileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                UserModel newUser = new UserModel(uid, email.getText().toString(), password.getText().toString(), nickName.getText().toString(), phoneNumber.getText().toString());
                profileURL = task.getResult().getDownloadUrl().toString();
                newUser.setProfileURL(profileURL);
                FirebaseDatabase.getInstance().getReference().child("Users").child(uid).setValue(newUser);
            }
        });

    }

    //회원양식 유효성 검사
    public boolean checkValidator(){
        boolean ret = true;

        if(email.getText().toString().trim() == null){
            Toast.makeText(SignUpActivity.this, "이메일을 작성해주세요", Toast.LENGTH_SHORT).show();
            ret = false;
        }
        if(password.getText().toString().trim() == null){
            Toast.makeText(SignUpActivity.this, "패스워드를 작성해주세요", Toast.LENGTH_SHORT).show();
            ret = false;
        }
        if(nickName.getText().toString().trim() == null){
            Toast.makeText(SignUpActivity.this, "닉네임을 작성해주세요", Toast.LENGTH_SHORT).show();
            ret = false;
        }
        if(phoneNumber.getText().toString().trim() == null){
            Toast.makeText(SignUpActivity.this, "전화번호를 작성해주세요", Toast.LENGTH_SHORT).show();
            ret = false;
        }
        if(profileUri == null){
            Toast.makeText(SignUpActivity.this, "프로필을 설정해주세요", Toast.LENGTH_SHORT).show();
            ret = false;
        }

        return ret;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK){

            profileUri = data.getData();
            profile.setImageURI(profileUri); // 사진 프로필 에 저장

        }

    }

}

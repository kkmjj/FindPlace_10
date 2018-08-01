package com.hanium.findplace.findplace_10.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.hanium.findplace.findplace_10.LoginActivity;
import com.hanium.findplace.findplace_10.R;
import com.hanium.findplace.findplace_10.models.UserModel;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

public class MyAccountFragment extends Fragment {

    //변수
    UserModel currentUser;
    private TextView Nickname;
    private TextView email;
    private TextView password;
    private TextView number;
    private TextView address;
    private ImageView delete;
    private Button change;
    private Button logout;
    private ImageView profile;
    private Uri profileUri;
    private String profileURL;
    private static final int PICK_FROM_ALBUM = 10;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_account, container, false);

        // 구성
        email = (TextView) view.findViewById(R.id._email);
        Nickname = (TextView) view.findViewById(R.id._nickname);
        password = (TextView) view.findViewById(R.id._password);
        address=(TextView)view.findViewById(R.id._address);
        number = (TextView) view.findViewById(R.id._number);

        delete = (ImageView) view.findViewById(R.id.delete);
        change =(Button)view.findViewById(R.id._change);
        logout=(Button)view.findViewById(R.id.MyAccount_Button_logout);

        profile=(ImageView)view.findViewById(R.id.MyAccountFragment_profile);





//
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(UserModel.class);
                        // 여기에 정보들 넣기
                        if (currentUser != null) {
                            Nickname.setText(currentUser.getNickName());
                            email.setText(currentUser.getEmail());
                            password.setText(currentUser.getPassword());
                            number.setText(currentUser.getPhoneNumber()); //화면에 정보 보여짐
                            //화면에 사진 보여짐
                            Glide.with(getContext())
                                    .load(currentUser.getProfileURL())
                                    .apply(new RequestOptions().circleCrop())
                                    .into(profile);
                        }

                        //프로필 클릭시
                        profile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_PICK);// 사진 고르게 해줌
                                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                                startActivityForResult(intent, PICK_FROM_ALBUM);




                            }
                        });





/*
                        //삭제 버튼 눌럿을시
                        delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {// 삭제 버튼을 눌렀을시 계정 삭제

                                // 데이터베이스 부분 삭제
                                FirebaseDatabase.getInstance().getReference().child("Users").
                                        child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().
                                        addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Intent intent = new Intent(getContext(), LoginActivity.class); // 회원가입창으로 넘어감
                                                startActivity(intent);
                                                getActivity().finish();
                                            }



                                        });


                                // AUTH 부분 삭제
                                FirebaseAuth.getInstance().getCurrentUser().delete().
                                        addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "User account deleted.");
                                                }
                                            }
                                        });

                                //사진 부분 삭제
                                FirebaseStorage.getInstance().getReference().child("profileImages").
                                        putFile(currentUser.getProfileURL()).removeOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    }
                                });
                                    //


                            }
                        });

                        //


*/

                        // change 버튼을 누르게 되면
                        change.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder ad = new AlertDialog.Builder(getContext());

                                ad.setTitle("비밀번호 변경");       // 제목 설정
                                ad.setMessage("변경할 비밀번호를 입력하세요");   // 내용 설정

// EditText 삽입하기
                                final EditText et = new EditText(getContext());
                                // 새로운 비번 별로 만들기
                                et.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                ad.setView(et);

// 확인 버튼 설정
                                ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Log.v(TAG, "Yes Btn Click");

                                        // Text 값 받아서 로그 남기기
                                        final String value = et.getText().toString();
                                        Log.v(TAG, value);

                                        // Auth에서 업데이트
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                        user.updatePassword(value)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {


                                                            // 데이터베이스에서 업데이트
                                                            Map<String, Object> tmpMap = new HashMap<>();
                                                            tmpMap.put("password", value);
                                                            FirebaseDatabase.getInstance().getReference().
                                                                    child("Users").child(currentUser.getUid()).updateChildren(tmpMap);
                                                            Log.d(TAG, "User password updated.");
                                                        }
                                                    }
                                                });

                                        dialog.dismiss();     //닫기
                                        // Event
                                    }


                                });

// 취소 버튼 설정
                                ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.v(TAG, "No Btn Click");
                                        dialog.dismiss();     //닫기
                                        // Event
                                    }
                                });
                                ad.show();

                            }
                        });

                        // 로그아웃 버튼 누르면
                        logout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("pushToken").removeValue();
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                // 회원가입창으로 넘어감
                                startActivity(intent);
                                getActivity().finish();
                            }
                        });








                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });






        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK){

            profileUri = data.getData();
            //profile.setImageURI(profileUri);        // 프로필 화면에서만 변경
            FirebaseStorage.getInstance().getReference().child("profileImages").putFile(profileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    profileURL = task.getResult().getDownloadUrl().toString();

                   // data 베이스 변경
                    Map<String, Object> tmpMap = new HashMap<>();
                    tmpMap.put("profileURL", profileURL);
                    FirebaseDatabase.getInstance().getReference().
                            child("Users").child(currentUser.getUid()).updateChildren(tmpMap);
                    Log.d(TAG, "User image updated.");
                }
            });
        }

    }

}

package com.hanium.findplace.findplace_10;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.hanium.findplace.findplace_10.models.UserModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CreateRoomActivity extends AppCompatActivity {

    private TextView search;
    private ImageButton createRoom;
    private EditText roomName;
    private RelativeLayout relativeLayout;
    private Button goBack;
    Map<String, UserModel> selectList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        String splash_background = FirebaseRemoteConfig.getInstance().getString(getString(R.string.rc_background));
        getWindow().setStatusBarColor(Color.parseColor(splash_background));

        relativeLayout = (RelativeLayout) findViewById(R.id.CreateRoomActivity_LinearLayout);
        //relativeLayout.setBackgroundColor(Color.parseColor(splash_background));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.CreateRoomActivity_RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyCreatePeopleViewAdapter());

        search = (TextView) findViewById(R.id.CreateRoomActivity_TextView_search);
        roomName = (EditText) findViewById(R.id.CreateRoomActivity_EditText_roomName);
        goBack = (Button) findViewById(R.id.ChatActivity_ActionMenuView_Button_goBack);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        createRoom = (ImageButton) findViewById(R.id.CreateRoomActivity_Button_createRoom);
        createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<String> uidList = new ArrayList<>();
                uidList.addAll(selectList.keySet());

                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(CreateRoomActivity.this, R.anim.fromcentertoleft, R.anim.fromrighttocenter);
                Intent intent = new Intent(CreateRoomActivity.this, ChatActivity.class);
                intent.putStringArrayListExtra("uid", uidList);
                if(roomName.getText().toString().equals("")){
                    roomName.setText("방제목없음");
                }
                intent.putExtra("roomName", roomName.getText().toString());
                startActivity(intent, activityOptions.toBundle());
                finish();
            }
        });

    }

    public class MyCreatePeopleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> myFriendList;
        String currentUid;

        //constructor
        public MyCreatePeopleViewAdapter(){
            myFriendList = new ArrayList<>();
            selectList = new HashMap<>();
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d("myLog----------------", "currentUid : "+currentUid);

            myFriendList = new ArrayList<>();
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d("myLog----------------", "currentUid : "+currentUid);

            FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    myFriendList.clear();
                    UserModel tmpModel = dataSnapshot.getValue(UserModel.class);
                    Iterator<String> iterator = tmpModel.getFriendUidList().keySet().iterator();
                    while(iterator.hasNext()){
                        String key = iterator.next();
                        FirebaseDatabase.getInstance().getReference().child("Users").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                myFriendList.add(dataSnapshot.getValue(UserModel.class));
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_createroom, parent, false);

            return new MyCreatePeopleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

            final MyCreatePeopleViewHolder thisHolder =  ((MyCreatePeopleViewHolder)holder);

            Glide.with(holder.itemView.getContext()).load(myFriendList.get(position)
                    .getProfileURL()).apply(new RequestOptions().circleCrop())
                    .into(((MyCreatePeopleViewHolder)holder).profile);

            ((MyCreatePeopleViewHolder)holder).nickName.setText(myFriendList.get(position).getNickName());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(thisHolder.checkBox.isChecked()){
                        thisHolder.checkBox.setChecked(false);
                    }else{
                        thisHolder.checkBox.setChecked(true);
                    }
                }
            });

            ((MyCreatePeopleViewHolder)holder).checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if(isChecked){
                        //체크
                        selectList.put(myFriendList.get(position).getUid(), myFriendList.get(position));
                    }else{
                        //체크안됨
                        selectList.remove(myFriendList.get(position).getUid());
                    }

                }
            });

        }

        @Override
        public int getItemCount() {
            return myFriendList.size();
        }

        private class MyCreatePeopleViewHolder extends RecyclerView.ViewHolder {

            public ImageView profile;
            public TextView nickName;
            public LinearLayout underLine;
            public CheckBox checkBox;

            public MyCreatePeopleViewHolder(View view) {
                super(view);

                profile = (ImageView) view.findViewById(R.id.ViewPeople_ImageView_profile);
                nickName = (TextView) view.findViewById(R.id.ViewPeople_TextView_nickname);
                underLine = (LinearLayout) view.findViewById(R.id.ViewPeople_LinearLayout_underLine);
                checkBox = (CheckBox) view.findViewById(R.id.ViewCreateRoom_checkBox);

            }
        }
    }
}

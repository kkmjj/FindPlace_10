package com.hanium.findplace.findplace_10;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.hanium.findplace.findplace_10.models.ChatModel;
import com.hanium.findplace.findplace_10.models.UserModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private RelativeLayout typingBox;
    private ImageButton send;
    private EditText typeText;

    private ChatModel chatModel;
    private String myUid;
    private String chatRoomUid;

    private RecyclerView recyclerView;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String splash_background = firebaseRemoteConfig.getString(getString(R.string.rc_background));
        getWindow().setStatusBarColor(Color.parseColor(splash_background));



        typingBox = (RelativeLayout) findViewById(R.id.ChatActivity_RelativeLayout_TypingBox);
        typingBox.setBackgroundColor(Color.parseColor(splash_background));
        typeText = (EditText) findViewById(R.id.ChatActivity_EditText_typeText);
        send = (ImageButton) findViewById(R.id.ChatActivity_ImageButton_send);

        Intent intent = getIntent();

        String destinationUid = intent.getStringExtra("uid");
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        checkValidator(myUid, destinationUid);

        //send Message!
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send.setEnabled(false);
                ChatModel.Comments newMessage = new ChatModel.Comments(myUid, typeText.getText().toString(), new Date(System.currentTimeMillis()));
                if(chatRoomUid == null){
                    Log.d("myLog-------------", "룸uid가 등록이 안되어잇음!!");
                }else{
                    FirebaseDatabase.getInstance().getReference().child("ChatRooms").child(chatRoomUid).child("Comments").push().setValue(newMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            send.setEnabled(true);
                            typeText.setText("");

                        }
                    });
                }


            }
        });



    }

    //참여자 유저정보 가져오기
    public void setParticipants(){



    }

    //채팅방 중복 방지
    public void checkValidator(final String myUid, final String destinationUid){

        //내가 속해있는 ChatRooms들 다 검색
        FirebaseDatabase.getInstance().getReference().child("ChatRooms").orderByChild("users_uid/"+myUid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //내가 속한 대화방 DB가 아무것도 없을때 생성.
                if(dataSnapshot.getValue() == null){
                    chatModel = new ChatModel();
                    chatModel.getUsers_uid().put(destinationUid, true);
                    chatModel.getUsers_uid().put(myUid, true);
                    FirebaseDatabase.getInstance().getReference().child("ChatRooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkValidator(myUid, destinationUid);
                        }
                    });
                    return;
                }
                boolean isjoongBok = true;
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    ChatModel model = dataSnapshot1.getValue(ChatModel.class);

                    //만약 내가 속해있는 ChatRooms들 중에서 1:1대화를 원하는 상대가 속해있고 그 대화방이 2명인지 확인 -- 즉 기존의 1:1 대화방이 존재하는지 체크
                    if(model.getUsers_uid().containsKey(destinationUid) && model.getUsers_uid().size() == 2){
                        //내가 속해있는 대화방이 이미 존재함.
                        chatRoomUid = dataSnapshot1.getKey();
                        chatModel = model;
                        isjoongBok = false;

                        recyclerView = (RecyclerView) findViewById(R.id.ChatActivity_RecyclerView);
                        recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
                        recyclerView.setAdapter(new MyChatRecyclerView());

                        break;
                    }
                }

                if(isjoongBok){
                    chatModel = new ChatModel();
                    chatModel.getUsers_uid().put(destinationUid, true);
                    chatModel.getUsers_uid().put(myUid, true);
                    FirebaseDatabase.getInstance().getReference().child("ChatRooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkValidator(myUid, destinationUid);
                        }
                    });
                    return;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //채팅화면 띄우기
    class MyChatRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<ChatModel.Comments> chatComments;
        List<UserModel> chat_participants;
        Map<String, UserModel> searchMapforUID;

        //Constructor
        public MyChatRecyclerView(){
            chatComments = new ArrayList<>();
            chat_participants = new ArrayList<>();
            searchMapforUID = new HashMap<>();

            FirebaseDatabase.getInstance().getReference().child("ChatRooms").child(chatRoomUid).child("users_uid").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    chat_participants.clear();

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        FirebaseDatabase.getInstance().getReference().child("Users").child(snapshot.getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                chat_participants.add(dataSnapshot.getValue(UserModel.class));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    FirebaseDatabase.getInstance().getReference().child("ChatRooms").child(chatRoomUid).child("Comments").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            chatComments.clear();
                            searchMapforUID.clear();

                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                chatComments.add(snapshot.getValue(ChatModel.Comments.class));
                            }



                            for(int i = 0; i < chatComments.size(); i++){

                                for(int j = 0; j < chat_participants.size(); j++){

                                    if(chatComments.get(i).uid.equals(chat_participants.get(j).getUid())){

                                        searchMapforUID.put(chatComments.get(i).uid, chat_participants.get(j));

                                    }
                                }
                            }

                            notifyDataSetChanged();
                            recyclerView.scrollToPosition(chatComments.size()-1);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message, parent, false);

            return new MyChatRecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            if(chat_participants.size() == 0){
                Log.d("myTag---------------", "chat참여자의 정보가 제대로 저장되지 않았음.");
            }else{

                MyChatRecyclerViewHolder thisHolder = ((MyChatRecyclerViewHolder)holder);

                Date date = chatComments.get(position).sendTime;
                SimpleDateFormat transFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                String strDate = transFormat.format(date);

                if(chatComments.get(position).uid.equals(myUid)){
                    //내 말풍선 설정
                    thisHolder.linearLayout_main.setGravity(Gravity.RIGHT);
                    thisHolder.linearLayout_yourProfile.setVisibility(View.INVISIBLE);
                    thisHolder.linearLayout_textBox.setGravity(Gravity.RIGHT);
                    thisHolder.chatDate.setText(strDate);
                    thisHolder.chatMessage.setText(chatComments.get(position).comments);
                    thisHolder.chatMessage.setBackgroundResource(R.drawable.mychat);
                }else{
                    //상대방 말풍선 설정
                    thisHolder.linearLayout_main.setGravity(Gravity.LEFT);
                    thisHolder.linearLayout_yourProfile.setVisibility(View.VISIBLE);
                    Glide.with(thisHolder.itemView.getContext())
                            .load(searchMapforUID.get(chatComments.get(position).uid).getProfileURL())
                            .apply(new RequestOptions().circleCrop()).into(thisHolder.yourProfile);
                    thisHolder.yourNickName.setText(searchMapforUID.get(chatComments.get(position).uid).getNickName());
                    thisHolder.linearLayout_textBox.setGravity(Gravity.LEFT);
                    thisHolder.chatDate.setText(strDate);
                    thisHolder.chatMessage.setText(chatComments.get(position).comments);
                    thisHolder.chatMessage.setBackgroundResource(R.drawable.yourchat);
                }

            }

        }

        @Override
        public int getItemCount() {
            return chatComments.size();
        }

        private class MyChatRecyclerViewHolder extends RecyclerView.ViewHolder {

            public LinearLayout linearLayout_main;
            public LinearLayout linearLayout_yourProfile;
            public ImageView yourProfile;
            public TextView yourNickName;

            public LinearLayout linearLayout_textBox;
            public TextView chatDate;
            public TextView chatMessage;


            public MyChatRecyclerViewHolder(View view) {
                super(view);

                linearLayout_main = (LinearLayout) view.findViewById(R.id.viewMessage_LinearLayout_main);
                linearLayout_yourProfile = (LinearLayout) view.findViewById(R.id.viewMessage_LinearLayout_yourProfile);
                yourProfile = (ImageView) view.findViewById(R.id.viewMessage_ImageView_yourProfile);
                yourNickName = (TextView) view.findViewById(R.id.viewMessage_TextView_yourNickName);

                linearLayout_textBox = (LinearLayout) view.findViewById(R.id.viewMessage_LinearLayout_textBox);
                chatDate = (TextView) view.findViewById(R.id.viewMessage_TextView_date);
                chatMessage = (TextView) view.findViewById(R.id.viewMessage_TextView_message);

            }
        }
    }

}

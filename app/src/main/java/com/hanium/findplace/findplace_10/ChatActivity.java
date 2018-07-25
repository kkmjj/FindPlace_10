package com.hanium.findplace.findplace_10;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.hanium.findplace.findplace_10.models.ChatModel;
import com.hanium.findplace.findplace_10.models.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    public final String MYTAG = "MYLOG__________________";
    public static final int CHATROOM_INDIVIDUAL = 2;
    public static final int CHATROOM_GROUP = 3;
    private int roomState;

    private final String SERVER_KEY = "AAAAxL9Z5kc:APA91bEdPSrJQG105Tmww7sDnX7suvq7XNwPt1h" +
            "RoXvLJmKLMJfPY1hZnOfRIpSbdE2yV9aUt9ZDvOnPefJgdLW97r2VA3hPew5su76RWVr4QrdtTqLihs" +
            "d4NLNFHnmwc5DyVnSRKVXNB8vJcJEEHK68DY6ZkyoZmw";

    private RelativeLayout typingBox;
    private ImageButton send;
    private EditText typeText;
    private LinearLayout actionMenuView;
    private Button actionMenuView_goBack;
    private TextView actionMenuView_chatRoomName;
    private Button actionMenuView_menu;

    private String typeTextBuffer;

    private String myUid;
    private String chatRoomUid;
    private String destinationUid;
    private ArrayList<String> destinationUids;
    private String roomName;

    private ChatModel chatModel;
    private UserModel myUserInfo;
    private String destinationToken;
    private Map<String, String> destinationTokens;
    //private List<UserModel> chat_participants;
    private Map<String, UserModel> searchMapforUID;

    private RecyclerView recyclerView;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    //-----------------------------------------------------DRAWERLAYOUT MEMBERVARIABLES
    private DrawerLayout drawerLayout;
    private Button drawerLayout_turnBack;
    private LinearLayout drawerLayout_inviteNewMember;
    private RecyclerView drawerLayout_recyclerview_memberList;

    //-----------------------------------------------------DRAWERLAYOUT MEMBERVARIABLES
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawerlayout_chat);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String splash_background = firebaseRemoteConfig.getString(getString(R.string.rc_background));
        getWindow().setStatusBarColor(Color.parseColor(splash_background));

        typingBox = (RelativeLayout) findViewById(R.id.ChatActivity_RelativeLayout_TypingBox);
        typeText = (EditText) findViewById(R.id.ChatActivity_EditText_typeText);
        send = (ImageButton) findViewById(R.id.ChatActivity_ImageButton_send);

        actionMenuView = (LinearLayout) findViewById(R.id.ChatActivity_ActionMenuView);
        actionMenuView_goBack = (Button) findViewById(R.id.ChatActivity_ActionMenuView_Button_goBack);
        actionMenuView_goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //채팅방 상단바 제목 설정.
        actionMenuView_chatRoomName = (TextView) findViewById(R.id.ChatActivity_ActionMenuView_TextView_chatRoomName);

        //설정해줘야함.
        actionMenuView_menu = (Button) findViewById(R.id.ChatActivity_ActionMenuView_Button_menu);
        actionMenuView_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //채팅상단바 메뉴 구현해야함!!
                if(!drawerLayout.isDrawerOpen(Gravity.RIGHT)){
                    drawerLayout.openDrawer(Gravity.RIGHT);
                    drawerLayout_recyclerview_memberList.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
                    drawerLayout_recyclerview_memberList.setAdapter(new MyGroupMemberShow());
                }
            }
        });

        //roomState check
        Intent intent = getIntent();
        if (intent.getStringArrayListExtra("uid") != null) {
            //이 방은 새로 생성한 단체방임 - createRoomActivity 를 통해 생성된것.
            roomState = CHATROOM_GROUP;
            destinationUids = intent.getStringArrayListExtra("uid");
            roomName = intent.getStringExtra("roomName");
        } else if (intent.getStringExtra("roomUid") != null) {
            //이 방은 기존에 존재하던 단체방임 - chatListFragment 에서 넘겨줄때 roomUid로 단체방으로 넘겨줌
            roomState = CHATROOM_GROUP;
            chatRoomUid = intent.getStringExtra("roomUid");
            destinationUids = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("ChatRooms").child(chatRoomUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ChatModel tmpChatModel = dataSnapshot.getValue(ChatModel.class);
                    for(String uid :tmpChatModel.users_uid.keySet()){
                        destinationUids.add(uid);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            //이 방은 개인방임 - chatList에서 이미 생성된 방에 들어오거나, 유저목록에서 1:1대화를 신청한 방임.
            roomState = CHATROOM_INDIVIDUAL;
            destinationUid = intent.getStringExtra("uid");
            FirebaseDatabase.getInstance().getReference().child("Users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String destinationNickName = dataSnapshot.getValue(UserModel.class).getNickName();
                    actionMenuView_chatRoomName.setText(destinationNickName);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //setting myUserInfo
        FirebaseDatabase.getInstance().getReference().child("Users").child(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        myUserInfo = dataSnapshot.getValue(UserModel.class);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        if(roomState == CHATROOM_INDIVIDUAL){
            checkValidator_individual(myUid, destinationUid);
        }else if(roomState == CHATROOM_GROUP){
            checkValidator_group(myUid, destinationUids);
        }else{
            Log.d("MyLog ===========", "checkValidator를 그냥 지나쳐감!!!!!!!!! roomState가 individual도 group도 아닌 상태임!!");
        }

        //send Message!
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (typeText.getText().toString().equals("")) {
                    return;
                }

                send.setEnabled(false);
                typeTextBuffer = typeText.getText().toString();
                typeText.setText("");
                ChatModel.Comments newMessage = new ChatModel.Comments(myUid, typeTextBuffer);
                newMessage.sendTime = ServerValue.TIMESTAMP;

                if (chatRoomUid == null) {
                    Log.d("myLog-------------", "룸uid가 등록이 안되어잇음!!");
                } else {
                    FirebaseDatabase.getInstance().getReference().child("ChatRooms").child(chatRoomUid).child("users_comments")
                            .push()
                            .setValue(newMessage)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    send.setEnabled(true);
                                    pushAlarm(typeTextBuffer);

                                }
                            });
                }
            }
        });

        //------------------------------------------------------------------------DRAWERLAYOUT MENU SETTINGS------------------------------------------
        drawerLayout = (DrawerLayout) findViewById(R.id.DrawerLayout_chatMenuLayout);
        drawerLayout_turnBack = (Button) findViewById(R.id.DrawerLayout_Button_turnBack);
        drawerLayout_turnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(drawerLayout.isDrawerOpen(Gravity.RIGHT)){
                    drawerLayout.closeDrawer(Gravity.RIGHT);
                }
            }
        });
        drawerLayout_inviteNewMember = (LinearLayout) findViewById(R.id.DrawerLayout_LinearLayout_inviteNewMember);
        drawerLayout_inviteNewMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {




                //새로운 멤버 초대화면으로 전환




            }
        });

        drawerLayout_recyclerview_memberList = (RecyclerView) findViewById(R.id.DrawerLayout_RecyclerView);

        //------------------------------------------------------------------------DRAWERLAYOUT MENU SETTINGS------------------------------------------

    }

    //setDestination Token / 단체대화방일때 유저들 uid갱신이 있을 경우 토큰값또한 재 갱신한다
    public void setDestinationToken() {

        if(roomState == CHATROOM_INDIVIDUAL){
            //개인대화방일 시

            FirebaseDatabase.getInstance().getReference().child("Users").child(destinationUid)
                    .child("pushToken").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    destinationToken = dataSnapshot.getValue(String.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else if(roomState == CHATROOM_GROUP){
            //단체대화방일 시
            destinationTokens = new HashMap<>();
            Log.d(MYTAG, "아니 혹시 uids사이즈가??? : "+destinationUids.size());
            FirebaseDatabase.getInstance().getReference().child("ChatRooms").child(chatRoomUid).child("users_uid")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            destinationTokens.clear();
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                final String usersTmpUid = snapshot.getKey();
                                Log.d(MYTAG, usersTmpUid+"새로 갱신된 유저UID");
                                FirebaseDatabase.getInstance().getReference().child("Users").child(usersTmpUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        UserModel tmpModel = dataSnapshot.getValue(UserModel.class);
                                        destinationTokens.put(usersTmpUid, tmpModel.getPushToken());
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
    }

    public void makePush(String destinationToken, String typeTextBuffer){
        JSONObject root = new JSONObject();
        try {
            // FMC 메시지 생성 start

            JSONObject notification = new JSONObject();
            JSONObject data = new JSONObject();
            //notification.put("body", typeTextBuffer);
            //notification.put("title", myUserInfo.getNickName());
            data.put("body", typeTextBuffer);
            data.put("title", myUserInfo.getNickName());
            //root.put("notification", notification);
            root.put("data", data);
            root.put("to", destinationToken);
            // FMC 메시지 생성 end

        } catch (JSONException e) {

        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), root.toString());
        Request request = new Request.Builder()
                .header("Content-type", "application/json")
                .addHeader("Authorization", "key=" + SERVER_KEY)
                .addHeader("Accept", "application/json")
                .url("https:fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    //대화 상대방에게 푸시메세지 보내기
    public void pushAlarm(String typeTextBuffer) {

        if(roomState == CHATROOM_INDIVIDUAL){
            //개인일 경우
            makePush(destinationToken, typeTextBuffer);
        }else if(roomState == CHATROOM_GROUP){
            Log.d("MyLog-----------", "푸시 destinationTokens Size : "+destinationTokens.size());
            for(int i = 0; i < destinationUids.size(); i++){
                Log.d("MyLog-------------", "푸쉬destinationTokens("+destinationUids.get(i)+") : "+destinationTokens.get(destinationUids.get(i)));
                makePush(destinationTokens.get(destinationUids.get(i)), typeTextBuffer);
            }
        }
    }

    //그룹 채팅방 중복 방지
    private void checkValidator_group(final String myUid, final ArrayList<String> destinationUids) {
        //먼저 그룹 채팅방이 새로 생성된 것인지 기존에 있는 방인지 파악해야한다
        if(chatRoomUid != null){
            FirebaseDatabase.getInstance().getReference().child("ChatRooms").child(chatRoomUid).child("roomName").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    actionMenuView_chatRoomName.setText(dataSnapshot.getValue(String.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            //인텐트에서 Uid를 받아온 것이기때문에 chatList에서 넘어온 기존에 존재한 방임.
            FirebaseDatabase.getInstance().getReference().child("ChatRooms")
                    .child(chatRoomUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    chatModel = dataSnapshot.getValue(ChatModel.class);
                    setDestinationToken();
                    //recyclerView 시작하는 공간임+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                    recyclerView = (RecyclerView) findViewById(R.id.ChatActivity_RecyclerView);
                    recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
                    recyclerView.setAdapter(new MyChatRecyclerView());

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else{
            //인텐트에서 아직 chatRoomUid를 받아오지 않은 생성되지 않은 방이다. 생성해야함.
            chatModel = new ChatModel();
            chatModel.users_uid.put(myUid, true);
            for(int i = 0; i < destinationUids.size(); i++){
                chatModel.users_uid.put(destinationUids.get(i), true);
            }
            chatModel.individualOrGroup = CHATROOM_GROUP;
            chatModel.roomName = roomName;
            FirebaseDatabase.getInstance().getReference().child("ChatRooms")
                    .push().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    chatRoomUid = dataSnapshot.getKey();
                    FirebaseDatabase.getInstance().getReference().child("ChatRooms")
                            .child(chatRoomUid).setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            ChatModel.Comments newMessage = new ChatModel.Comments(myUid, myUserInfo.getNickName()+" 님이 "+roomName+" 모임을 생성하였습니다");
                            newMessage.sendTime = ServerValue.TIMESTAMP;

                            if (chatRoomUid == null) {
                                Log.d("myLog-------------", "룸uid가 등록이 안되어잇음!!");
                            } else {
                                FirebaseDatabase.getInstance().getReference().child("ChatRooms").child(chatRoomUid).child("users_comments")
                                        .push()
                                        .setValue(newMessage)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                send.setEnabled(true);
                                                pushAlarm(myUserInfo.getNickName()+" 님이 "+roomName+" 모임을 생성하였습니다");

                                            }
                                        });
                            }

                            checkValidator_group(myUid, destinationUids);
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

    }

    //개인 채팅방 중복 방지
    public void checkValidator_individual(final String myUid, final String destinationUid) {

        //내가 속해있는 ChatRooms들 다 검색
        FirebaseDatabase.getInstance().getReference().child("ChatRooms")
                .orderByChild("users_uid/" + myUid).equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //내가 속한 대화방 DB가 아무것도 없을때 생성.
                        if (dataSnapshot.getValue() == null) {
                            chatModel = new ChatModel();
                            chatModel.users_uid.put(destinationUid, true);
                            chatModel.users_uid.put(myUid, true);
                            chatModel.individualOrGroup = CHATROOM_INDIVIDUAL;
                            FirebaseDatabase.getInstance().getReference().child("ChatRooms")
                                    .push().setValue(chatModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            checkValidator_individual(myUid, destinationUid);
                                        }
                                    });
                            return;
                        }

                        boolean isjoongBok = true;

                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            ChatModel model = dataSnapshot1.getValue(ChatModel.class);

                            //만약 내가 속해있는 ChatRooms들 중에서 1:1대화를 원하는 상대가 속해있고 그 대화방이 2명인지 확인 -- 즉 기존의 1:1 대화방이 존재하는지 체크
                            if (model.users_uid.containsKey(destinationUid) && model.users_uid.size() == 2) {
                                //내가 속해있는 대화방이 이미 존재함.
                                chatRoomUid = dataSnapshot1.getKey();
                                chatModel = model;
                                isjoongBok = false;
                                setDestinationToken();

                                recyclerView = (RecyclerView) findViewById(R.id.ChatActivity_RecyclerView);
                                recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
                                recyclerView.setAdapter(new MyChatRecyclerView());

                                return;
                            }
                        }

                        //내와 1:1대화를 원하는 상대가 동시에 속해있는 방이 있기는 하나 단체방 밖에 없으므로 새로운 개인대화방 생성.
                        if (isjoongBok) {
                            chatModel = new ChatModel();
                            chatModel.users_uid.put(destinationUid, true);
                            chatModel.users_uid.put(myUid, true);
                            chatModel.individualOrGroup = CHATROOM_INDIVIDUAL;
                            FirebaseDatabase.getInstance().getReference().child("ChatRooms")
                                    .push().setValue(chatModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            checkValidator_individual(myUid, destinationUid);
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
    class MyChatRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<ChatModel.Comments> chat_commentsList;
        List<Integer> chat_readUserCount;

        //Constructor
        public MyChatRecyclerView() {

            chat_commentsList = new ArrayList<>();
            searchMapforUID = new HashMap<>();
            chat_readUserCount = new ArrayList<>();
            //chat_participants = new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("ChatRooms").child(chatRoomUid).child("users_uid").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        final String findUserForUid = snapshot.getKey();
                        FirebaseDatabase.getInstance().getReference().child("Users").child(findUserForUid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                searchMapforUID.put(findUserForUid, dataSnapshot.getValue(UserModel.class));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    databaseReference = FirebaseDatabase.getInstance().getReference().child("ChatRooms").child(chatRoomUid).child("users_comments");
                    valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            chat_commentsList.clear();
                            //searchMapforUID.clear();

                            Map<String, Object> readUserMap = new HashMap<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String key = snapshot.getKey();
                                ChatModel.Comments originComment = snapshot.getValue(ChatModel.Comments.class);
                                ChatModel.Comments modifyComment = snapshot.getValue(ChatModel.Comments.class);
                                modifyComment.readUsers.put(myUid, true);

                                readUserMap.put(key, modifyComment);
                                chat_commentsList.add(originComment);
                            }

                            if(chat_commentsList.size() == 0){

                            }else{

                                //readUsers 읽은 사람 수 설정
                                for (int i = 0; i < chat_commentsList.size(); i++) {
                                    chat_readUserCount.add(i, searchMapforUID.size() - chat_commentsList.get(i).readUsers.size());
                                }

                                if (chat_commentsList.get(chat_commentsList.size() - 1).readUsers.containsKey(myUid)) {
                                    //서버에 이미 한번 갱신된 정보(맨 마지막이 읽었다고 표시가 됨)
                                    notifyDataSetChanged();
                                    recyclerView.scrollToPosition(chat_commentsList.size() - 1);
                                } else {
                                    //마지막을 아직 읽지 않음
                                    FirebaseDatabase.getInstance().getReference().child("ChatRooms").child(chatRoomUid).child("users_comments")
                                            .updateChildren(readUserMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            notifyDataSetChanged();
                                            recyclerView.scrollToPosition(chat_commentsList.size() - 1);
                                        }
                                    });
                                }
                            }



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

            if (searchMapforUID.size() == 0) {
                Log.d("myTag---------------", "chat참여자의 정보가 제대로 저장되지 않았음.");
            } else {

                MyChatRecyclerViewHolder thisHolder = ((MyChatRecyclerViewHolder) holder);

                //시간설정
                long unixTime = (long) chat_commentsList.get(position).sendTime;
                Date date = new Date(unixTime);
                SimpleDateFormat transFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                transFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                String strDate;
                strDate = transFormat.format(date);

                if (chat_commentsList.get(position).uid.equals(myUid)) {
                    //내 말풍선 설정
                    thisHolder.linearLayout_main.setGravity(Gravity.RIGHT);
                    thisHolder.linearLayout_textBox.setGravity(Gravity.RIGHT);
                    thisHolder.yourNickName.setText("");
                    thisHolder.yourProfile.setVisibility(View.INVISIBLE);
                    thisHolder.chatDate.setText("");
                    thisHolder.chatDateRight.setText(strDate);
                    thisHolder.chatMessage.setText(chat_commentsList.get(position).message);
                    thisHolder.chatMessage.setBackgroundResource(R.drawable.mychat);
                    thisHolder.chatReadCountRight.setText("");
                    if (chat_readUserCount.get(position) == 0) {
                        thisHolder.chatReadCountLeft.setText("");
                    } else {
                        thisHolder.chatReadCountLeft.setText(String.valueOf(chat_readUserCount.get(position)));
                    }

                } else {
                    //상대방 말풍선 설정
                    thisHolder.linearLayout_main.setGravity(Gravity.LEFT);
                    thisHolder.yourProfile.setVisibility(View.VISIBLE);
                    thisHolder.yourNickName.setText(searchMapforUID.get(chat_commentsList.get(position).uid).getNickName());
                    Glide.with(thisHolder.itemView.getContext())
                            .load(searchMapforUID.get(chat_commentsList.get(position).uid).getProfileURL())
                            .apply(new RequestOptions().circleCrop()).into(thisHolder.yourProfile);
                    thisHolder.linearLayout_textBox.setGravity(Gravity.LEFT);
                    thisHolder.chatDate.setText(strDate);
                    thisHolder.chatDateRight.setText("");
                    thisHolder.chatMessage.setText(chat_commentsList.get(position).message);
                    thisHolder.chatMessage.setBackgroundResource(R.drawable.yourchat);
                    thisHolder.chatReadCountLeft.setText("");
                    if (chat_readUserCount.get(position) == 0) {
                        thisHolder.chatReadCountRight.setText("");
                    } else {
                        thisHolder.chatReadCountRight.setText(String.valueOf(chat_readUserCount.get(position)));
                    }
                }

            }

        }

        @Override
        public int getItemCount() {
            return chat_commentsList.size();
        }

        private class MyChatRecyclerViewHolder extends RecyclerView.ViewHolder {

            public LinearLayout linearLayout_main;
            public RelativeLayout RelativeLayout_yourProfile;
            public ImageView yourProfile;
            public TextView yourNickName;

            public LinearLayout linearLayout_textBox;
            public TextView chatDate;
            public TextView chatDateRight;
            public TextView chatMessage;
            public TextView chatReadCountRight;
            public TextView chatReadCountLeft;

            public MyChatRecyclerViewHolder(View view) {
                super(view);

                linearLayout_main = (LinearLayout) view.findViewById(R.id.viewMessage_LinearLayout_main);
                RelativeLayout_yourProfile = (RelativeLayout) view.findViewById(R.id.viewMessage_RelativeLayout_yourProfile);
                yourProfile = (ImageView) view.findViewById(R.id.viewMessage_ImageView_yourProfile);
                yourNickName = (TextView) view.findViewById(R.id.viewMessage_TextView_yourNickName);

                linearLayout_textBox = (LinearLayout) view.findViewById(R.id.viewMessage_LinearLayout_textBox);
                chatDate = (TextView) view.findViewById(R.id.viewMessage_TextView_date);
                chatDateRight = (TextView) view.findViewById(R.id.viewMessage_TextView_dateRight);
                chatMessage = (TextView) view.findViewById(R.id.viewMessage_TextView_message);
                chatReadCountRight = (TextView) view.findViewById(R.id.viewMessage_TextView_readCountRight);
                chatReadCountLeft = (TextView) view.findViewById(R.id.viewMessage_TextView_readCountLeft);

            }
        }
    }

    //DrawerLayout Menu에서 현재 채팅멤버 보여주기
    class MyGroupMemberShow extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> participants_list;

        public MyGroupMemberShow(){

            participants_list = new ArrayList<>();
            Iterator<String> iterator = searchMapforUID.keySet().iterator();
            while(iterator.hasNext()){

                String key = iterator.next();
                participants_list.add(searchMapforUID.get(key));

            }

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_group_member, parent, false);

            return new MyGroupMemberViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            if(searchMapforUID.size() == 0){
                Log.d(MYTAG, "chat_participants.size() 가 0이다...");
            }else{

                MyGroupMemberViewHolder thisHolder = (MyGroupMemberViewHolder)holder;
                thisHolder.nickName.setText(participants_list.get(position).getNickName());
                //thisHolder.nickName.setText("닉네임이다다ㅏ닫");
                Glide.with(thisHolder.itemView.getContext())
                        .load(participants_list.get(position).getProfileURL())
                        .apply(new RequestOptions().circleCrop()).into(thisHolder.profile);

            }

        }

        @Override
        public int getItemCount() {
            return searchMapforUID.size();
        }
    }

    private class MyGroupMemberViewHolder extends RecyclerView.ViewHolder {

        private ImageView profile;
        private TextView nickName;

        public MyGroupMemberViewHolder(View view) {
            super(view);

            profile = (ImageView) view.findViewById(R.id.ViewPeople_ImageView_profile);
            nickName = (TextView) view.findViewById(R.id.ViewPeople_TextView_nickname);

        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public void onRestart() {
        if (databaseReference == null || valueEventListener == null) {

        } else {
            databaseReference.addValueEventListener(valueEventListener);
        }
        super.onRestart();
    }

    @Override
    public void onResume() {
        if (databaseReference == null || valueEventListener == null) {

        } else {
            databaseReference.addValueEventListener(valueEventListener);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (databaseReference == null || valueEventListener == null) {

        } else {
            databaseReference.removeEventListener(valueEventListener);
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (databaseReference == null || valueEventListener == null) {

        } else {
            databaseReference.removeEventListener(valueEventListener);
        }
        super.onStop();
    }

    @Override
    public void onStart(){
        super.onStart();
    }


    @Override
    public void onDestroy() {
        if (databaseReference == null || valueEventListener == null) {

        } else {
            databaseReference.removeEventListener(valueEventListener);
        }
        super.onDestroy();
    }

}

package com.hanium.findplace.findplace_10.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hanium.findplace.findplace_10.ChatActivity;
import com.hanium.findplace.findplace_10.CreateRoomActivity;
import com.hanium.findplace.findplace_10.R;
import com.hanium.findplace.findplace_10.models.ChatModel;
import com.hanium.findplace.findplace_10.models.UserModel;
import com.hanium.findplace.findplace_10.navermap.MyNaverMapFragment;
import com.melnykov.fab.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class ChatListFragment extends Fragment {

    private FrameLayout mapView;
    private RecyclerView chatList_view;
    public static TextView myProfile;
    private FloatingActionButton floatingActionButton;

    public ChatListFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //네이버지도.
        MyNaverMapFragment myNaverMapFragment = new MyNaverMapFragment();
        myNaverMapFragment.setArguments(new Bundle());
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.ChatListActivity_FrameLayout_mapView, myNaverMapFragment);
        fragmentTransaction.commit();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        mapView = (FrameLayout) view.findViewById(R.id.ChatListActivity_FrameLayout_mapView);
        chatList_view = (RecyclerView) view.findViewById(R.id.ChatListActivity_RecyclerView_chatList);
        myProfile = (TextView) view.findViewById(R.id.ChatListActivity_TextView_address);
        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.ChatListActivity_FloatingActionButton_createChatRoom);

        chatList_view.setAdapter(new ChatListRecyclerViewAdapter());
        chatList_view.setLayoutManager(new LinearLayoutManager(getContext()));
        floatingActionButton.attachToRecyclerView(chatList_view);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CreateRoomActivity.class));
            }
        });

        return view;
    }

    public class ChatListRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private String myUid;
        private Map<Integer, String> chatRoomUid_map;
        private ArrayList<ChatModel> chatModelList;

        //constructor
        public ChatListRecyclerViewAdapter() {
            chatRoomUid_map = new HashMap<>();
            chatModelList = new ArrayList();
            myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference().child("ChatRooms").orderByChild("users_uid/" + myUid).equalTo(true).addValueEventListener(new ValueEventListener() {
                    @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    chatRoomUid_map.clear();
                    chatModelList.clear();
                    int i = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        ChatModel tmpModel = snapshot.getValue(ChatModel.class);
                        String roomUid = snapshot.getKey();
                        if(tmpModel.users_comments.isEmpty()){
                            continue;
                        }
                        chatModelList.add(i, tmpModel);
                        chatRoomUid_map.put(i, roomUid);
                        i++;
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_chatroom, parent, false);

            return new MyChatRoomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

            final MyChatRoomViewHolder thisHolder = ((MyChatRoomViewHolder) holder);

            //uidList == 현재 대화방에 속한 상대방 uid 리스트
            final List<String> uidList = new ArrayList<>();

            for (String uid : chatModelList.get(position).users_uid.keySet()) {
                if (!(uid.equals(myUid))) {
                    uidList.add(uid);
                }
            }

            if (uidList.size() == 1 && !chatModelList.get(position).users_comments.isEmpty() && chatModelList.get(position).individualOrGroup == ChatActivity.CHATROOM_INDIVIDUAL) {
                //1대1 대화방
                Log.d("MyLoG__________________", "아이씨 1:1대화방으로 if문이 드어감");
                FirebaseDatabase.getInstance().getReference().child("Users").child(uidList.get(0)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserModel counterUser = dataSnapshot.getValue(UserModel.class);

                        thisHolder.roomName.setText(counterUser.getNickName());
                        Glide.with(thisHolder.itemView.getContext()).load(counterUser.getProfileURL()).apply(new RequestOptions().circleCrop()).into(thisHolder.profile);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //개신기함.
                Map<String, ChatModel.Comments> chatMap = new TreeMap<>(Collections.reverseOrder());
                chatMap.putAll(chatModelList.get(position).users_comments);
                String lastComments = (String) chatMap.keySet().toArray()[0];
                thisHolder.innerText.setText(chatModelList.get(position).users_comments.get(lastComments).message);

                int unReadCount = 0;
                for(int i = 0; i < chatMap.size(); i++){
                    String currentComments = (String) chatMap.keySet().toArray()[i];
                    if(chatModelList.get(position).users_comments.get(currentComments).readUsers.containsKey(myUid)){
                        break;
                    }else{
                        unReadCount++;
                    }
                }
                if(unReadCount == 0){
                    thisHolder.unReadCount.setText("");
                }else{
                    thisHolder.unReadCount.setText(String.valueOf(unReadCount));
                }


                long unixTime = (long) chatModelList.get(position).users_comments.get(lastComments).sendTime;
                Date date = new Date(unixTime);
                SimpleDateFormat transFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                transFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                String strDate = transFormat.format(date);

                thisHolder.sendTime.setText(strDate);
                thisHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ChatActivity.class);
                        intent.putExtra("uid", uidList.get(0));
                        startActivity(intent);
                    }
                });

            } else if(chatModelList.get(position).individualOrGroup == ChatActivity.CHATROOM_GROUP){
                //단체대화방 (추후 설정) || intent 값넘길때 putExtra("roomUid", roomUid) 로 넘길것!!!
                //현재 chatModel 의 roomUid = chatRoomUid_map.get(position)
                Log.d("MyLoG__________________", "엥? 단체대화방으로 if문이 드어감");
                //1대1 대화방

                thisHolder.roomName.setText(chatModelList.get(position).roomName);
                Glide.with(thisHolder.itemView.getContext())
                        .load("https://firebasestorage.googleapis.com/v0/b/findplace10.appspot.com/o/groupIcon.png?alt=media&token=7dc10f21-1dd8-4e3c-9422-fa2ece669742")
                        .apply(new RequestOptions().circleCrop()).into(thisHolder.profile);

                //개신기함.
                Map<String, ChatModel.Comments> chatMap = new TreeMap<>(Collections.reverseOrder());
                chatMap.putAll(chatModelList.get(position).users_comments);
                String lastComments = (String) chatMap.keySet().toArray()[0];
                thisHolder.innerText.setText(chatModelList.get(position).users_comments.get(lastComments).message);

                int unReadCount = 0;
                for(int i = 0; i < chatMap.size(); i++){
                    String currentComments = (String) chatMap.keySet().toArray()[i];
                    if(chatModelList.get(position).users_comments.get(currentComments).readUsers.containsKey(myUid)){
                        break;
                    }else{
                        unReadCount++;
                    }
                }
                if(unReadCount == 0){
                    thisHolder.unReadCount.setText("");
                }else{
                    thisHolder.unReadCount.setText(String.valueOf(unReadCount));
                }

                long unixTime = (long) chatModelList.get(position).users_comments.get(lastComments).sendTime;
                Date date = new Date(unixTime);
                SimpleDateFormat transFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                transFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                String strDate = transFormat.format(date);

                thisHolder.sendTime.setText(strDate);
                thisHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ChatActivity.class);
                        intent.putExtra("roomUid", chatRoomUid_map.get(position));
                        startActivity(intent);
                    }
                });

            }

        }

        @Override
        public int getItemCount() {
            return chatRoomUid_map.size();
        }

        private class MyChatRoomViewHolder extends RecyclerView.ViewHolder {

            public ImageView profile;
            public TextView roomName;
            public TextView innerText;
            public TextView sendTime;
            public TextView unReadCount;
            public LinearLayout linearLayout;

            public MyChatRoomViewHolder(View view) {
                super(view);

                profile = (ImageView) view.findViewById(R.id.ViewChatRoom_ImageView_profile);
                roomName = (TextView) view.findViewById(R.id.ViewChatRoom_TextView_roomName);
                innerText = (TextView) view.findViewById(R.id.ViewChatRoom_TextView_text);
                sendTime = (TextView) view.findViewById(R.id.ViewChatRoom_TextView_time);
                unReadCount = (TextView) view.findViewById(R.id.ViewChatRoom_TextView_count);
                linearLayout = (LinearLayout) view.findViewById(R.id.ViewChatRoom_LinearLayout);

            }
        }
    }

}

package com.hanium.findplace.findplace_10.fragment;


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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hanium.findplace.findplace_10.R;
import com.hanium.findplace.findplace_10.models.ChatModel;
import com.hanium.findplace.findplace_10.models.UserModel;
import com.hanium.findplace.findplace_10.navermap.MyNaverMapFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChatListFragment extends Fragment {

    private FrameLayout mapView;
    private RecyclerView chatList;
    public static TextView myProfile;

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
        chatList = (RecyclerView) view.findViewById(R.id.ChatListActivity_RecyclerView_chatList);
        myProfile = (TextView) view.findViewById(R.id.ChatListActivity_TextView_address);

        chatList.setAdapter(new ChatListRecyclerViewAdapter());
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));


        return view;
    }

    public class ChatListRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private String myUid;
        private List<ChatModel> chatList;

        //constructor
        public ChatListRecyclerViewAdapter() {
            chatList = new ArrayList<>();
            myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference().child("ChatRooms").orderByChild("users_uid/" + myUid).equalTo(true).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    chatList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ChatModel tmpModel = snapshot.getValue(ChatModel.class);
                        if(tmpModel.users_comments.isEmpty()){
                            continue;
                        }
                        chatList.add(tmpModel);
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
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            final MyChatRoomViewHolder thisHolder = ((MyChatRoomViewHolder) holder);

            List<String> uidList = new ArrayList<>();

            for (String uid : chatList.get(position).users_uid.keySet()) {
                if (!(uid.equals(myUid))) {
                    uidList.add(uid);
                }
            }

            if (uidList.size() == 1 && !chatList.get(position).users_comments.isEmpty()) {
                //1대1 대화방
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
                chatMap.putAll(chatList.get(position).users_comments);
                String lastComments = (String) chatMap.keySet().toArray()[0];
                thisHolder.innerText.setText(chatList.get(position).users_comments.get(lastComments).message);
                thisHolder.sendTime.setText(chatList.get(position).users_comments.get(lastComments).sendTime);

            } else {
                //단체대화방 (추후 설정)




            }

        }

        @Override
        public int getItemCount() {
            return chatList.size();
        }

        private class MyChatRoomViewHolder extends RecyclerView.ViewHolder {

            public ImageView profile;
            public TextView roomName;
            public TextView innerText;
            public TextView sendTime;
            public TextView unReadCount;

            public MyChatRoomViewHolder(View view) {
                super(view);

                profile = (ImageView) view.findViewById(R.id.ViewChatRoom_ImageView_profile);
                roomName = (TextView) view.findViewById(R.id.ViewChatRoom_TextView_roomName);
                innerText = (TextView) view.findViewById(R.id.ViewChatRoom_TextView_text);
                sendTime = (TextView) view.findViewById(R.id.ViewChatRoom_TextView_time);
                unReadCount = (TextView) view.findViewById(R.id.ViewChatRoom_TextView_count);

            }
        }
    }

}

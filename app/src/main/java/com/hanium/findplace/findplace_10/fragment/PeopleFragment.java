package com.hanium.findplace.findplace_10.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.hanium.findplace.findplace_10.R;
import com.hanium.findplace.findplace_10.ProfileActivity;
import com.hanium.findplace.findplace_10.models.UserModel;

import java.util.ArrayList;
import java.util.List;

public class PeopleFragment extends Fragment {

    String splash_background;

    public PeopleFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people, container, false);

        splash_background = FirebaseRemoteConfig.getInstance().getString(getString(R.string.rc_background));


        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.PeopleFragment_RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new MyPeopleViewAdapter());

        return view;
    }

    public class MyPeopleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> userList;
        String currentUid;

        //constructor
        public MyPeopleViewAdapter(){
            userList = new ArrayList<>();
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d("myLog----------------", "currentUid : "+currentUid);

            FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    userList.clear();
                    UserModel tmpModel;
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        tmpModel = snapshot.getValue(UserModel.class);
                        if(tmpModel.getUid().equals(currentUid)){
                            userList.add(0, tmpModel);
                        }else{
                            userList.add(tmpModel);
                        }

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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_people, parent, false);

            return new MyPeopleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

            Glide.with(holder.itemView.getContext()).load(userList.get(position)
                    .getProfileURL()).apply(new RequestOptions().circleCrop())
                    .into(((MyPeopleViewHolder)holder).profile);

            ((MyPeopleViewHolder)holder).nickName.setText(userList.get(position).getNickName());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(getContext(), R.anim.fromcentertoleft, R.anim.fromrighttocenter);
                    Intent intent = new Intent(getContext(), ProfileActivity.class);
                    intent.putExtra("uid", userList.get(position).getUid());
                    startActivity(intent, activityOptions.toBundle());
                }
            });

        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        private class MyPeopleViewHolder extends RecyclerView.ViewHolder {

            public ImageView profile;
            public TextView nickName;
            public LinearLayout underLine;

            public MyPeopleViewHolder(View view) {
                super(view);

                profile = (ImageView) view.findViewById(R.id.ViewPeople_ImageView_profile);
                nickName = (TextView) view.findViewById(R.id.ViewPeople_TextView_nickname);
                underLine = (LinearLayout) view.findViewById(R.id.ViewPeople_LinearLayout_underLine);
                underLine.setBackgroundColor(Color.parseColor(splash_background));

            }
        }
    }

}

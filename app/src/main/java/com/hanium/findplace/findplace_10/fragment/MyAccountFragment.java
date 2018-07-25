package com.hanium.findplace.findplace_10.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hanium.findplace.findplace_10.LoginActivity;
import com.hanium.findplace.findplace_10.R;
import com.hanium.findplace.findplace_10.models.UserModel;

public class MyAccountFragment extends Fragment {

    private TextView myNickName;

    private String myUid;

    private UserModel myAccount;
    private Button logout;

    public MyAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_account, container, false);

        myNickName = (TextView) view.findViewById(R.id.MyAccount_TextView_nickName);
        logout = (Button) view.findViewById(R.id.MyAccount_Button_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Users").child(myUid).child("pushToken").removeValue();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), LoginActivity.class));
                getActivity().finish();
            }
        });

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference().child("Users").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                myAccount = dataSnapshot.getValue(UserModel.class);
                myNickName.setText(myAccount.getNickName());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

}

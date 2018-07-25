package com.hanium.findplace.findplace_10;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hanium.findplace.findplace_10.models.UserModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakeFriendsActivity extends AppCompatActivity {

    private static final String MYTAG = "MYLOG______________";

    private Spinner materialBetterSpinner;
    private static final String[] SEARCH = new String[] {
            "Category", "E-mail", "Phone"
    };

    private EditText inputCategory;
    private RecyclerView recyclerView;
    private Button addFriends;
    private Button cancel;

    private List<UserModel> searchResultModel;
    private Map<String, Object> friendUidList;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);
        setContentView(R.layout.activity_make_friends);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.dropdownlayout, SEARCH);

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        inputCategory = (EditText) findViewById(R.id.MakeFriends_EditText_inputCategory);
        materialBetterSpinner = (Spinner) findViewById(R.id.MakeFriends_BetterSpinner);
        materialBetterSpinner.setAdapter(adapter);
        materialBetterSpinner.setFocusable(true);
        materialBetterSpinner.setFocusableInTouchMode(true);
        materialBetterSpinner.requestFocus();
        materialBetterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.d(MYTAG, "일단 onItemSeleted안으로 들어오긴 함");
                //입력해야함!!
                if(parent.getItemAtPosition(position).toString().equals("E-mail")){
                    Log.d(MYTAG, "이메일 클릭함!!!!!!!!!!!");
                    inputCategory.setFocusable(true);
                    inputCategory.requestFocus();
                    inputCategory.setHint("유저검색 : 이메일");
                }else if(parent.getItemAtPosition(position).equals("Phone")){
                    inputCategory.setFocusable(true);
                    inputCategory.requestFocus();
                    inputCategory.setHint("유저검색 : 휴대폰");
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        searchResultModel = new ArrayList<>();
        friendUidList = new HashMap<>();
        recyclerView = (RecyclerView) findViewById(R.id.MakeFriends_RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(MakeFriendsActivity.this));
        recyclerView.setAdapter(new ShowUserListAdapter());

        addFriends = (Button) findViewById(R.id.MakeFriends_Button_ok);
        addFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseDatabase.getInstance().getReference().child("Users").child(myUid).child("friendUidList").updateChildren(friendUidList);
                finish();
            }
        });

        cancel = (Button) findViewById(R.id.MakeFriends_Button_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private class ShowUserListAdapter extends RecyclerView.Adapter {

        //constructor
        public ShowUserListAdapter(){
            //실시간 검색하기.
            inputCategory.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    final String searchValue = inputCategory.getText().toString();
                    Log.d(MYTAG, "onTextChanged 변경점 진입");
                    if(materialBetterSpinner.getSelectedItem().toString().equals("E-mail") && !searchValue.equals("")){
                        Log.d(MYTAG, "Email 조건에 부합하여 파이어베이스 검색까지 들어옴.");
                        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                searchResultModel.clear();
                                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                                    UserModel tmpModel = snapshot.getValue(UserModel.class);
                                    if(tmpModel.getEmail().contains(searchValue) && !tmpModel.getUid().equals(myUid)){
                                        searchResultModel.add(tmpModel);
                                        Log.d(MYTAG, "Email에 searchResultModel 하나 넣음");
                                    }
                                }
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }else if(materialBetterSpinner.getSelectedItem().toString().equals("Phone") && !searchValue.equals("")){
                        Log.d(MYTAG, "Phone 조건에 부합하여 파이어베이스 검색까지 들어옴.");
                        FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                searchResultModel.clear();
                                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                                    UserModel tmpModel = snapshot.getValue(UserModel.class);
                                    if(tmpModel.getPhoneNumber().contains(searchValue) && !tmpModel.getUid().equals(myUid)){
                                        searchResultModel.add(tmpModel);
                                        Log.d(MYTAG, "Phone에 searchResultModel 하나 넣음");
                                    }
                                }
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_find_friends, parent, false);

            return new ShowUserListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

            ShowUserListViewHolder thisHolder = ((ShowUserListViewHolder) holder);

            if(searchResultModel == null){
                Log.d(MYTAG, "검색모델에 입력된 정보가 없음");
            }else{
                thisHolder.nickName.setText(searchResultModel.get(position).getNickName());
                Glide.with(thisHolder.itemView.getContext()).load(searchResultModel.get(position).getProfileURL())
                        .apply(new RequestOptions().circleCrop()).into(thisHolder.profile);

                thisHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        if(isChecked){
                            //체크되었을때

                            friendUidList.put(searchResultModel.get(position).getUid(), true);

                        }else{
                            //체크안되었을때

                            friendUidList.remove(searchResultModel.get(position).getUid());

                        }

                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return searchResultModel.size();
        }

        private class ShowUserListViewHolder extends RecyclerView.ViewHolder {

            //member variables
            public CheckBox checkBox;
            public ImageView profile;
            public TextView nickName;

            //constructor
            public ShowUserListViewHolder(View view) {
                super(view);

                checkBox = (CheckBox) view.findViewById(R.id.MakeFriends_CheckBox);
                profile = (ImageView) view.findViewById(R.id.MakeFriends_ImageView_profile);
                nickName = (TextView) view.findViewById(R.id.MakeFriends_TextView_nickName);

            }
        }
    }

}


package com.hanium.findplace.findplace_10;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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


        addFriends = (Button) findViewById(R.id.MakeFriends_Button_ok);
        cancel = (Button) findViewById(R.id.MakeFriends_Button_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);



    }
}


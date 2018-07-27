package com.hanium.findplace.findplace_10;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;

public class MakeAppointmentActivity extends AppCompatActivity {

    //member variables
    public final String MYTAG = "MyLog_____________";
    private Spinner materialBetterSpinner;
    private static final String[] SEARCH = new String[] {
            "Category", "카페", "식당", ""
    };

    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_appointment);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.dropdownlayout, SEARCH);

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

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

                }else if(parent.getItemAtPosition(position).equals("Phone")){

                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
}

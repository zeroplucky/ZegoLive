package com.mindaxx.myapplicaton;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private EditText mRoomName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    public void onLoginClick(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            } else {
                startSessionActivity();
            }
        } else {
            startSessionActivity();
        }
    }

    private void startSessionActivity() {
        Intent startIntent = new Intent(MainActivity.this, ZegoPhoneActivity.class);
        startIntent.putExtra("roomId", mRoomName.getText().toString().trim());
        startActivity(startIntent);

//        Intent intent = new Intent(MainActivity.this, VideoActivity.class);
//        intent.putExtra("roomId", mRoomName.getText().toString().trim());
//        startActivity(intent);
    }

    private void initView() {
        mRoomName = (EditText) findViewById(R.id.room_name);
    }


}

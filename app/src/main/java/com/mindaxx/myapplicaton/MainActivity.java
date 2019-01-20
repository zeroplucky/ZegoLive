package com.mindaxx.myapplicaton;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText mRoomName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    public void onAudioClick(View view) {
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
        String roomId = mRoomName.getText().toString().trim();
        if (TextUtils.isEmpty(roomId)) {
            Toast.makeText(this, "房间号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent startIntent = new Intent(MainActivity.this, AudioActivity.class);
        startIntent.putExtra("roomId", roomId);
        startActivity(startIntent);
    }

    public void onVideoClick(View view) {
        String roomId = mRoomName.getText().toString().trim();
        if (TextUtils.isEmpty(roomId)) {
            Toast.makeText(this, "房间号不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MainActivity.this, VideoActivity.class);
        intent.putExtra("roomId", roomId);
        startActivity(intent);
    }

    private void initView() {
        mRoomName = (EditText) findViewById(R.id.room_name);
    }


}

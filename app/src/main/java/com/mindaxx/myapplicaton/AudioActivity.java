package com.mindaxx.myapplicaton;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mindaxx.zegolib.ZegoAudio;


public class AudioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        ZegoAudio.with().initZego(getApplicationContext());

        String roomId = getIntent().getStringExtra("roomId");
        ZegoAudio.with().login(roomId);

        ZegoAudio.with().startPublish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoAudio.with().destroy();
    }
}

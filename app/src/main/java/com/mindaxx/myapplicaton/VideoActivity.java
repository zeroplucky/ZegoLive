package com.mindaxx.myapplicaton;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mindaxx.zegolib.PrefUtil;
import com.mindaxx.zegolib.TimeUtil;
import com.mindaxx.zegolib.ZegoVideo;
import com.zego.zegoliveroom.ZegoLiveRoom;
import com.zego.zegoliveroom.callback.IZegoLoginCompletionCallback;
import com.zego.zegoliveroom.callback.IZegoRoomCallback;
import com.zego.zegoliveroom.constants.ZegoConstants;
import com.zego.zegoliveroom.constants.ZegoVideoViewMode;
import com.zego.zegoliveroom.entity.ZegoStreamInfo;

public class VideoActivity extends AppCompatActivity {

    private FrameLayout bigVideoLiveView;
    private FrameLayout smallView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 禁止手机休眠
        initView();
        ZegoVideo.instance.setupZegoSDK(getApplication());  // 初始化
        ZegoVideo.instance.setZegoRoomCallback(zegoRoomIinstener); // 监听房间
        loginRoom(); // 登录
    }

    private void initView() {
        bigVideoLiveView = (FrameLayout) findViewById(R.id.big_view);
        smallView = (FrameLayout) findViewById(R.id.small_view);
    }

    private void loginRoom() {
        String sessionId = getIntent().getStringExtra("roomId");
        ZegoVideo.getLiveRoom().loginRoom(sessionId, ZegoConstants.RoomRole.Audience, new ZegoLgoinCompleteCallback());
    }


    private class ZegoLgoinCompleteCallback implements IZegoLoginCompletionCallback {
        @Override
        public void onLoginCompletion(int errorCode, ZegoStreamInfo[] streamList) {
            if (isFinishing()) return;
            ZegoVideo.instance.startPublishStream(VideoActivity.this, bigVideoLiveView); // 推流
            ZegoVideo.instance.startPlayStreams(VideoActivity.this, streamList, bigVideoLiveView, smallView); //如果房间有流，就播流
            Log.e("xxx", "onLoginCompletion:  2");
        }
    }

    private ZegoVideo.ZegoRoomIinstener zegoRoomIinstener = new ZegoVideo.ZegoRoomIinstener() {
        @Override
        public void onStreamUpdated(int type, ZegoStreamInfo[] streamList, String roomId) {
            if (type == ZegoConstants.StreamUpdateType.Added) {
                ZegoVideo.instance.startPlayStreams(VideoActivity.this, streamList, bigVideoLiveView, smallView);
            } else if (type == ZegoConstants.StreamUpdateType.Deleted) {
                ZegoVideo.instance.stopPlayStreams(VideoActivity.this, streamList, bigVideoLiveView, smallView);
            } else {
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoVideo.instance.logoutRoom();
    }


}
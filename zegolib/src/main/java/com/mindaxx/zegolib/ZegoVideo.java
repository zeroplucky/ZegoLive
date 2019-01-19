package com.mindaxx.zegolib;

//import com.zego.zegoliveroom.ZegoLiveRoom;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.zego.zegoliveroom.ZegoLiveRoom;
import com.zego.zegoliveroom.callback.IZegoRoomCallback;
import com.zego.zegoliveroom.constants.ZegoAvConfig;
import com.zego.zegoliveroom.constants.ZegoConstants;
import com.zego.zegoliveroom.constants.ZegoVideoViewMode;
import com.zego.zegoliveroom.entity.ZegoStreamInfo;

import java.lang.ref.PhantomReference;

/**
 * Created by Administrator on 2019/1/19.
 */

public class ZegoVideo {

    public static ZegoVideo instance = new ZegoVideo();
    private ZegoLiveRoom mZegoLiveRoom;
    private Context context;
    public ZegoRoomIinstener zegoRoomIinstener;

    public static void saveLiveRoom(ZegoLiveRoom instance) {
        ZegoVideo.instance.mZegoLiveRoom = instance;
    }

    public static ZegoLiveRoom getLiveRoom() {
        if (instance.mZegoLiveRoom == null) {
            instance.mZegoLiveRoom = new ZegoLiveRoom();
        }
        return instance.mZegoLiveRoom;
    }


    public void setupZegoSDK(final Application application) {
        context = application;
        initUserInfo();

        ZegoLiveRoom liveRoom = ZegoVideo.getLiveRoom();
        ZegoLiveRoom.setSDKContext(new ZegoLiveRoom.SDKContext() {
            @Override
            public String getSoFullPath() {
                return null;
            }

            @Override
            public String getLogPath() {
                return null;
            }

            @Override
            public Application getAppContext() {
                return application;
            }
        });
        initZegoSDK(liveRoom);
        ZegoVideo.saveLiveRoom(liveRoom);
    }

    public ZegoAvConfig config;

    private void initZegoSDK(ZegoLiveRoom liveRoom) {
        ZegoLiveRoom.setUser(PrefUtil.getInstance(context).getUserId(), PrefUtil.getInstance(context).getUserName());
        ZegoLiveRoom.requireHardwareEncoder(true);
        ZegoLiveRoom.requireHardwareDecoder(true);
        ZegoLiveRoom.setTestEnv(true);
        byte[] signKey = AppSignKey.signData_udp;
        long appId = AppSignKey.UDP_APP_ID;
        // 设置视频通话类型
        ZegoLiveRoom.setBusinessType(0);
        boolean success = liveRoom.initSDK(appId, signKey);
        liveRoom.setLatencyMode(ZegoConstants.LatencyMode.Low3);
        if (!success) {
        } else {
            config = new ZegoAvConfig(ZegoAvConfig.Level.High);
            liveRoom.setAVConfig(config);
        }
    }

    public void initUserInfo() {
        String userId = PrefUtil.getInstance(context).getUserId();
        String userName = PrefUtil.getInstance(context).getUserName();
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(userName)) {
            userId = TimeUtil.getNowTimeStr();
            userName = String.format("VT_%s_", userId);
            PrefUtil.getInstance(context).setUserId(userId);
            PrefUtil.getInstance(context).setUserName(userName);
        }
    }

    /*
    *  开始推流 并 播流
    * */
    public void startPublishStream(Activity activity, FrameLayout bigLayout) {
        String streamId = String.format("s-%s-%s", PrefUtil.getInstance(activity).getUserId(), TimeUtil.getNowTimeStr());
        String title = String.format("%s is comming", PrefUtil.getInstance(activity).getUserId());
        ZegoLiveRoom liveRoom = ZegoVideo.getLiveRoom();
        int currentOrientation = activity.getWindowManager().getDefaultDisplay().getRotation();
        liveRoom.setAppOrientation(currentOrientation);
        int properties = ZegoConstants.ZegoTrafficControlProperty.ZEGOAPI_TRAFFIC_FPS
                | ZegoConstants.ZegoTrafficControlProperty.ZEGOAPI_TRAFFIC_RESOLUTION;
        liveRoom.enableTrafficControl(properties, true);
        liveRoom.setVideoCodecId(ZegoConstants.ZegoVideoCodecAvc.VIDEO_CODEC_DEFAULT, ZegoConstants.PublishChannelIndex.MAIN);
        // 开始推流
        boolean success = liveRoom.startPublishing(streamId, title, ZegoConstants.PublishFlag.JoinPublish);
        Toast.makeText(activity, "推流状态：" + success, Toast.LENGTH_SHORT).show();
        //播流
        TextureView view = new TextureView(activity);
        if (bigLayout.getChildCount() > 1) {
            bigLayout.removeAllViews();
        }
        bigLayout.setVisibility(View.VISIBLE);
        bigLayout.addView(view);
        liveRoom.enableMic(true);
        liveRoom.enableCamera(true);
        liveRoom.enableSpeaker(true);
        liveRoom.setPreviewView(view);
        liveRoom.setPreviewViewMode(ZegoVideoViewMode.ScaleAspectFit);
        liveRoom.startPreview();
    }

    /*
    * 开始播流
    * */
    public void startPlayStreams(Activity activity, ZegoStreamInfo[] streamList, FrameLayout bigView, FrameLayout smallView) {
        if (streamList.length == 0) return;
        bigView.setVisibility(View.VISIBLE);
        smallView.setVisibility(View.VISIBLE);
        if (bigView.getChildCount() == 0) {
            bigView.addView(new TextureView(activity));
        }
        if (smallView.getChildCount() == 0) {
            smallView.addView(new TextureView(activity));
        }
        ZegoStreamInfo streamInfo = streamList[0];
        String streamId = streamInfo.streamID;
        ZegoLiveRoom liveRoom = ZegoVideo.getLiveRoom();
        liveRoom.startPlayingStream(streamId, null);
        liveRoom.activateVedioPlayStream(streamId, true, ZegoConstants.VideoStreamLayer.VideoStreamLayer_Auto);
        // 播远程流
        ZegoVideo.getLiveRoom().updatePlayView(streamInfo.streamID, bigView.getChildAt(0));
        ZegoVideo.getLiveRoom().setViewMode(ZegoVideoViewMode.ScaleAspectFit, streamInfo.streamID);
        //播本地流
        liveRoom.setPreviewView(smallView.getChildAt(0));
        liveRoom.setPreviewViewMode(ZegoVideoViewMode.ScaleAspectFit);
    }

    /*
    *
    * */
    public void stopPlayStreams(Activity activity, ZegoStreamInfo[] streamList, FrameLayout bigLayout, FrameLayout smallLayout) {
        ZegoStreamInfo streamInfo = streamList[0];
        String streamId = streamInfo.streamID;
        if (streamId == null) {
            return;
        }
        if (bigLayout.getChildCount() == 0) {
            bigLayout.addView(new TextureView(activity));
        }
        ZegoLiveRoom liveRoom = ZegoVideo.getLiveRoom();
        liveRoom.stopPlayingStream(streamId);

        // 将本地流设置到大View中
        liveRoom.setPreviewView(bigLayout.getChildAt(0));
        liveRoom.setPreviewViewMode(ZegoVideoViewMode.ScaleAspectFit);
        smallLayout.setVisibility(View.GONE);
    }

    /*
    *
    * */
    public void logoutRoom() {
        try {
            ZegoLiveRoom liveRoom = ZegoVideo.getLiveRoom();
            liveRoom.stopPublishing();
            liveRoom.stopPreview();
            liveRoom.logoutRoom();
            liveRoom.setZegoRoomCallback(null);
        } catch (Exception e) {

        }
    }

    public void setZegoRoomCallback(ZegoRoomIinstener zegoRoomIinstener) {
        if (this.zegoRoomIinstener == null) {
            this.zegoRoomIinstener = zegoRoomIinstener;
            getLiveRoom().setZegoRoomCallback(new ZegoRoomCallback());
        }
    }

    public interface ZegoRoomIinstener {
        void onStreamUpdated(int type, ZegoStreamInfo[] streamList, String roomId);
    }


    /*
  * 房间内监听
  * */
    private class ZegoRoomCallback implements IZegoRoomCallback {


        /**
         * 因为登陆抢占原因等被挤出房间
         */
        @Override
        public void onKickOut(int reason, String roomId) {
        }

        /**
         * 与 server 断开
         */
        @Override
        public void onDisconnect(int errorCode, String roomId) {
        }

        /**
         * 中断后重连
         */
        @Override
        public void onReconnect(int errorCode, String roomId) {
        }

        /**
         * 临时中断
         */
        @Override
        public void onTempBroken(int errorCode, String roomId) {
        }

        /**
         * 房间流列表更新
         */
        @Override
        public void onStreamUpdated(int type, ZegoStreamInfo[] streamList, String roomId) {
            if (zegoRoomIinstener != null) {
                zegoRoomIinstener.onStreamUpdated(type, streamList, roomId);
            }
//            if (type == ZegoConstants.StreamUpdateType.Added) {
//                ZegoVideo.instance.startPlayStreams(VideoActivity.this, streamList, bigVideoLiveView, smallView);
//            } else if (type == ZegoConstants.StreamUpdateType.Deleted) {
//                ZegoVideo.instance.stopPlayStreams(VideoActivity.this, streamList, bigVideoLiveView, smallView);
//            } else {
//            }
        }

        /**
         * 更新流的额外信息
         */
        @Override
        public void onStreamExtraInfoUpdated(ZegoStreamInfo[] streamList, String roomId) {

        }

        /**
         * 收到自定义消息
         */
        @Override
        public void onRecvCustomCommand(String fromUserId, String fromUserName, String content, String roomId) {

        }
    }


}

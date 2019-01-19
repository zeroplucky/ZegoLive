package com.mindaxx.zegolib;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.zego.zegoaudioroom.ZegoAudioLivePublisherDelegate;
import com.zego.zegoaudioroom.ZegoAudioPrepareDelegate;
import com.zego.zegoaudioroom.ZegoAudioRoom;
import com.zego.zegoaudioroom.ZegoAuxData;
import com.zego.zegoaudioroom.ZegoLoginAudioRoomCallback;
import com.zego.zegoliveroom.ZegoLiveRoom;
import com.zego.zegoliveroom.constants.ZegoConstants;
import com.zego.zegoliveroom.entity.ZegoExtPrepSet;
import com.zego.zegoliveroom.entity.ZegoStreamQuality;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Created by Administrator on 2019/1/18.
 */

public class ZegoAudio {

    private ZegoAudioRoom mZegoAudioRoom;
    private String userId;
    private String userName;
    private String TAG = " Zego xxx";
    Context context;
    private static volatile ZegoAudio zegoAudio;
    private boolean hasLogin = false;

    public static ZegoAudio with() {
        synchronized (ZegoAudio.class) {
            if (zegoAudio == null)
                zegoAudio = new ZegoAudio();
        }
        return zegoAudio;
    }

    public void initZego(Context context) {
        String userId = getUserId();
        String userName = getUserName();
        ZegoAudioRoom.setUser(userId, userName);
        ZegoAudioRoom.setUseTestEnv(true); // 正式环境 改为flase
        ZegoExtPrepSet config = new ZegoExtPrepSet();
        config.encode = false;
        config.channel = 0;
        config.sampleRate = 0;
        config.samples = 1;
        ZegoAudioRoom.enableAudioPrep2(true, config);
        mZegoAudioRoom = new ZegoAudioRoom();
        mZegoAudioRoom.setManualPublish(true); // 是否手动推流
        mZegoAudioRoom.setManualPlay(false); // 是否手动拉流
        ZegoAudioRoom.setBusinessType(0); //type - 0:直播
        boolean withAppId = mZegoAudioRoom.initWithAppId(AppSignKey.UDP_APP_ID, AppSignKey.signData_udp, context);
        Log.e(TAG, "initZego: " + withAppId);
        mZegoAudioRoom.setLatencyMode(ZegoConstants.LatencyMode.Low3);

    }

    public void reInitZegoSDK(Context context) {
        if (mZegoAudioRoom != null) {
            mZegoAudioRoom.unInit();
        }
        initZego(context);
    }

    private String getUserId() {
        userId = System.currentTimeMillis() / 1000 + "";
        return userId;
    }

    private String getUserName() {
        userName = System.currentTimeMillis() + getUserId();
        return userName;
    }

    public ZegoAudioRoom getZegoClient() {
        ZegoLiveRoom liveRoomInstance = mZegoAudioRoom.getLiveRoomInstance();
        return mZegoAudioRoom;
    }

    public ZegoLiveRoom getZegoLiveRoom() {
        ZegoLiveRoom.setBusinessType(2);
        return mZegoAudioRoom.getLiveRoomInstance();
    }

    /*
    * 进入房间
    * */
    public void login(String roomId) {
        // TODO 开始登陆房间
        setupCallbacks();
        mZegoAudioRoom.setBuiltinSpeakerOn(true);
        mZegoAudioRoom.setUserStateUpdate(true);
        mZegoAudioRoom.enableAux(false);
        mZegoAudioRoom.enableMic(true);
        mZegoAudioRoom.enableSelectedAudioRecord(ZegoConstants.AudioRecordMask.NoRecord, 44100);
        mZegoAudioRoom.enableSpeaker(true);
        mZegoAudioRoom.loginRoom(roomId, new ZegoLoginAudioRoomCallback() {
            @Override
            public void onLoginCompletion(int state) {
                if (state == 0) {
                    // TODO 登陆成功
                    hasLogin = true;
                } else {
                    Log.e(TAG, "onLoginCompletion:  登录失败 , ecode = " + state);
                }
            }
        });
    }

    /*
    * 开始推流
    * */
    public void startPublish() {
        mZegoAudioRoom.startPublish();
    }

    /*
    * 结束推流
    * */
    public void stopPublish() {
        mZegoAudioRoom.stopPublish();
    }

    /*
    * 退出
    * */
    public void destroy() {
        if (hasLogin) {
            boolean success = mZegoAudioRoom.logoutRoom();
            hasLogin = false;
        }
        removeCallbacks();
    }

    final int restart_publsh_msg = 1;

    private Handler restartHandler = new Handler() {

        int restartCount = 0;

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == restart_publsh_msg) {
                //计数器
                if (restartCount <= 10) {
                    restartCount++;
                    boolean restartState = mZegoAudioRoom.restartPublishStream();
                }
            }
        }
    };

    /*
    * 设置回调
    * */
    private void setupCallbacks() {
        mZegoAudioRoom.setAudioPublisherDelegate(new ZegoAudioLivePublisherDelegate() {
            @Override
            public void onPublishStateUpdate(int stateCode, String streamId, HashMap<String, Object> info) {
                if (stateCode == 0) {

                } else {
                    // TODO 推流失败 //延时10秒后开启重新推流
                    restartHandler.removeMessages(restart_publsh_msg);
                    restartHandler.sendMessageDelayed(restartHandler.obtainMessage(restart_publsh_msg), 10000);
                }
            }

            @Override
            public ZegoAuxData onAuxCallback(int dataLen) {
                return null;
            }

            @Override
            public void onPublishQualityUpdate(String streamId, ZegoStreamQuality zegoStreamQuality) {
            }
        });


        mZegoAudioRoom.setAudioPrepareDelegate(new ZegoAudioPrepareDelegate() {
            private long lastCallbackTime = 0;

            @Override
            public void onAudioPrepared(ByteBuffer inData, int sampleCount, int bitDepth, int sampleRate, ByteBuffer outData) {
                long nowTime = System.currentTimeMillis();
                if (nowTime - lastCallbackTime > 1000) {    // 过滤不停回调显示太多日志，只需要有一条日志表示有回调就可以了
                }
                lastCallbackTime = nowTime;
                if (inData != null && outData != null) {
                    inData.position(0);
                    outData.position(0);
                    // 不可更改
                    outData.limit(sampleCount * bitDepth);
                    // 将处理后的数据返回sdk
                    outData.put(inData);
                }
            }
        });
    }

    private void removeCallbacks() {
        mZegoAudioRoom.setAudioRoomDelegate(null);
        mZegoAudioRoom.setAudioPublisherDelegate(null);
        mZegoAudioRoom.setAudioPlayerDelegate(null);
        mZegoAudioRoom.setAudioLiveEventDelegate(null);
        mZegoAudioRoom.setAudioRecordDelegate(null);
        mZegoAudioRoom.setAudioDeviceEventDelegate(null);
        mZegoAudioRoom.setAudioPrepareDelegate(null);
        mZegoAudioRoom.setAudioAVEngineDelegate(null);
        if (restartHandler != null) {
            restartHandler.removeCallbacksAndMessages(null);
        }
    }
}

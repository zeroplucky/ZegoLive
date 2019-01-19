package com.mindaxx.zegolib;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PrefUtil {

    static private PrefUtil sInstance;

    static private String KEY_USER_ID = "_zego_user_id";
    static private String KEY_USER_NAME = "_zego_user_name";
    private SharedPreferences mPref;

    private PrefUtil(Context context) {
        mPref = context.getSharedPreferences("__global_pref_v3", Context.MODE_PRIVATE);
    }

    static public PrefUtil getInstance(Context context) {
        if (sInstance == null) {
            synchronized (PrefUtil.class) {
                if (sInstance == null) {
                    sInstance = new PrefUtil(context);
                }
            }
        }
        return sInstance;
    }

    private PrefUtil setInt(String key, int value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt(key, value);
        editor.apply();
        return this;
    }

    private PrefUtil setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
        return this;
    }

    private PrefUtil setLong(String key, long value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putLong(key, value);
        editor.apply();
        return this;
    }

    private PrefUtil setString(String key, String value) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(key, value);
        editor.apply();
        return this;
    }

    private PrefUtil setObject(String key, Object value) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            String textData = new String(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));

            setString(key, textData);
        } catch (IOException e) {
            e.printStackTrace();
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
        return this;
    }

    private Object getObject(String key) {
        Object value = null;
        ByteArrayInputStream bais = null;
        try {
            String rawValue = mPref.getString(key, null);
            if (rawValue != null) {
                byte[] rawBytes = Base64.decode(rawValue, Base64.DEFAULT);
                bais = new ByteArrayInputStream(rawBytes);
                ObjectInputStream ois = new ObjectInputStream(bais);
                value = ois.readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (bais != null) {
                try {
                    bais.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
        return value;
    }

    public String getUserId() {
        return mPref.getString(KEY_USER_ID, "");
    }

    public void setUserId(String userId) {
        setString(KEY_USER_ID, userId);
    }

    public String getUserName() {
        return mPref.getString(KEY_USER_NAME, "");
    }

    public void setUserName(String userName) {
        setString(KEY_USER_NAME, userName);
    }


}

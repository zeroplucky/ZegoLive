package com.mindaxx.zegolib;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

    static final private SimpleDateFormat sFormat = new SimpleDateFormat();

    static public String getNowTimeStr() {
        sFormat.applyPattern("yyMMddHHmmssSSS");
        return sFormat.format(new Date());
    }

    static public String getLogStr() {
        sFormat.applyPattern("HH:mm:ss.SSS");
        return sFormat.format(new Date());
    }
}

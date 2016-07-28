package com.panxiong.instant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.panxiong.instant.config.UrlConfig;
import com.panxiong.instant.model.Users;
import com.panxiong.instant.receiver.AlarmBroadcastReceiver;
import com.panxiong.instant.service.CoreService;
import com.panxiong.instant.service.GuardService;
import com.panxiong.instant.utils.PairUtil;
import com.panxiong.instant.utils.StrUtil;

/**
 * Created by panxi on 2016/6/27.
 * <p>
 * 初始化入口
 */
public class InstantSDK {
    public static final long ALARM_PERIOD_TIME = 5 * 60 * 1000L;    // 闹钟重复间隔时间
    public static volatile boolean closeService = false;    // 关闭服务
    public static volatile String RECEIVER_KEY = "";   // 广播字符
    public static volatile Users loginUser = null;  // 当前登录用户

    private static volatile PendingIntent alarmPendingIntent = null; // 闹钟

    public synchronized static void init(
            @NonNull Context context, @NonNull String address, @NonNull Integer port) {
        init(context, address, port, null, null, null);
    }

    public synchronized static void init(
            @NonNull Context context, @NonNull String address, @NonNull Integer port, String userId, String pass, String receiverKey) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 0,
                new Intent(context, AlarmBroadcastReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                ALARM_PERIOD_TIME, ALARM_PERIOD_TIME, alarmPendingIntent);
        if (!StrUtil.isEmpty(receiverKey)) {
            InstantSDK.RECEIVER_KEY = receiverKey;
        }
        if (!StrUtil.isEmpty(userId) && !StrUtil.isEmpty(pass)) {
            PairUtil.saveString(context, "USER_ID", userId);
            PairUtil.saveString(context, "USER_PW", pass);
        }

        if (!StrUtil.isEmpty(address) && !StrUtil.isEmpty(port.toString())) {
            UrlConfig.setAddress(context, address);
            UrlConfig.setPort(context, port.longValue());

            InstantSDK.closeService = false;
            context.startService(new Intent(context, CoreService.class));
            context.startService(new Intent(context, GuardService.class));
        }
    }

    public synchronized static void close(@NonNull Context context) {
        /*清空用户信息*/
        PairUtil.saveString(context, "USER_ID", "-1");
        PairUtil.saveString(context, "USER_PW", "-1");
        /*初始化计时信息*/
        GuardService.keepTime = -1L;
        /*关闭定时器*/
        if (alarmPendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(alarmPendingIntent);
        }
        /*停止Service与守护进程*/
        InstantSDK.closeService = true;
        context.stopService(new Intent(context, GuardService.class));
        context.stopService(new Intent(context, CoreService.class));
        /*清空登陆信息*/
        InstantSDK.loginUser = null;
    }

}

package com.panxiong.instant.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.panxiong.instant.InstantSDK;
import com.panxiong.instant.utils.PairUtil;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by panxi on 2016/6/14.
 * <p/>
 * Socket连接守护进程
 */
public class GuardService extends Service {
    public static final long SERVICE_PERIOD = 3 * 60 * 1000L;   // 与服务器约定的时间
    public static volatile long keepTime = -1L;
    private volatile Timer timer = null;

    @Override
    public IBinder onBind(Intent intent) {
        throw new RuntimeException("守护进程不可以绑定");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_TICK");  // 时间流逝
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");    // 电池的电量发生变化
        registerReceiver(new GuardBroadcastReceiver(), intentFilter);

        registerReceiver(new KeepBroadcastReceiver(), new IntentFilter("com.panxiong.instant.KEEP"));   // 更新连接时间广播
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String userId = PairUtil.getString(this, "USER_ID");
        String pass = PairUtil.getString(this, "USER_PW");
        if (!"-1".equals(userId.trim()) && !"-1".equals(pass.trim())) {   // 不是初始化运行
            if (!isServiceWork(this, CoreService.class.getName())) {
                startService(new Intent(GuardService.this, CoreService.class));    // 重新打开连接
                return START_STICKY;
            }
            if (timer != null) timer.cancel();
            timer = new Timer();    // 系统的闹钟是在4.4后计时是不准的 建立一个自己的计时器辅助计时
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (keepTime != -1L) {
                        long timeout = System.currentTimeMillis() - keepTime - 1000;    // 允许误差
                        if (timeout > SERVICE_PERIOD) {    // 是否超时
                            startService(new Intent(GuardService.this, CoreService.class));  // 再次启动Service
                            Log.d("GuardService", "服务断开 正在重连");
                        }
                    }
                }
            }, 0, InstantSDK.ALARM_PERIOD_TIME);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent("com.receiver.SERVICE_DESTROY");
        intent.putExtra("SERVICE_NAME", this.getClass().getName());
        sendBroadcast(intent);
    }

    /*判断某个服务是否正在运行的方法*/
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

    /*更新连接时间*/
    static class KeepBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            GuardService.keepTime = System.currentTimeMillis();
        }
    }

    /*监听时间/电池广播*/
    static class GuardBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (InstantSDK.closeService) return;
            context.startService(new Intent(context, GuardService.class)); // 重启服务
        }
    }

}

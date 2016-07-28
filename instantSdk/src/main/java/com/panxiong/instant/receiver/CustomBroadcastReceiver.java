package com.panxiong.instant.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.panxiong.instant.InstantSDK;
import com.panxiong.instant.service.GuardService;

/**
 * Created by panxi on 2016/6/16.
 * <p>
 * 监听网络改变广播
 */
public class CustomBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Log.e("CustomBroadcastReceiver", "广播动作： " + intent.getAction().trim());

        if (InstantSDK.closeService || !netConn(context)) return;
        context.startService(new Intent(context, GuardService.class)); // 重启服务
    }

    /* 检查网络状态的 */
    public static boolean netConn(Context context) {
        NetworkInfo netInfo = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (null != netInfo && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}

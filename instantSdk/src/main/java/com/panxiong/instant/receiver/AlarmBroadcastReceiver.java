package com.panxiong.instant.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.panxiong.instant.service.GuardService;

/**
 * Created by panxi on 2016/6/16.
 * <p/>
 * 监听定时器闹钟
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context.getApplicationContext(), GuardService.class));  // 通过闹钟更新守护进程
    }
}

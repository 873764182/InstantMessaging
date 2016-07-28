package com.panxiong.instant.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.panxiong.instant.InstantSDK;
import com.panxiong.instant.service.CoreService;
import com.panxiong.instant.service.GuardService;

/**
 * Created by panxi on 2016/6/27.
 * <p>
 * Service销毁监听
 */
public class ServiceDestroyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra("SERVICE_NAME");
        if (type != null && !InstantSDK.closeService) {
            if (CoreService.class.getName().equals(type)) {
                context.startService(new Intent(context.getApplicationContext(), CoreService.class));
            } else if (GuardService.class.getName().equals(type)) {
                context.startService(new Intent(context.getApplicationContext(), GuardService.class));
            }
        }
    }
}

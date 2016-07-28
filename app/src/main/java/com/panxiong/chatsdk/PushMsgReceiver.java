package com.panxiong.chatsdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.panxiong.instant.model.PushMsg;

/**
 * Created by panxi on 2016/7/13.
 *
 * 接收推送消息
 */
public class PushMsgReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent i) {
        PushMsg pushMsg = (PushMsg) i.getSerializableExtra("PUSH_MSG");
        if (pushMsg == null) return;

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        int nId = (int) (Math.random() * 10000);    // 每次ID必须不一样
        Intent intent = new Intent(context, NoticeActivity.class);
        intent.putExtra("PUSH_MSG", pushMsg);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, nId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_notifica);
        remoteViews.setTextViewText(R.id.text_title, pushMsg.pushTitle);
        remoteViews.setTextViewText(R.id.text_content, pushMsg.pushContent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContent(remoteViews);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setTicker("收到通知");  // 带上移动画效果的
        builder.setWhen(System.currentTimeMillis());
        builder.setPriority(Notification.PRIORITY_DEFAULT);
        builder.setAutoCancel(true);    // 点击后消息消失
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();

        notificationManager.notify(nId, notification);
    }
}

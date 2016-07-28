package com.panxiong.chatsdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.panxiong.instant.model.PushMsg;

/**
 * 显示推送消息
 */
public class NoticeActivity extends AppCompatActivity {
    private TextView textMessag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        textMessag = (TextView) findViewById(R.id.text_messag);

        Intent intent = getIntent();
        if (intent == null) return;
        PushMsg msgData = (PushMsg) intent.getSerializableExtra("PUSH_MSG");
        if (msgData == null) return;
        textMessag.setText(msgData.toString());
    }
}

package com.panxiong.chatsdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.panxiong.instant.InstantSDK;
import com.panxiong.instant.activity.InstantUsersActivity;

public class MainActivity extends AppCompatActivity {
    private EditText user_address;
    private EditText mUserId;
    private EditText mUserPass;
    private TextView mTextMsg;
    private Button mBtnInit;
    private Button mBtnUsers;
    private Button mBtnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user_address = (EditText) findViewById(R.id.user_address);
        mUserId = (EditText) findViewById(R.id.user_id);
        mUserPass = (EditText) findViewById(R.id.user_pass);
        mTextMsg = (TextView) findViewById(R.id.text_msg);
        mBtnInit = (Button) findViewById(R.id.btn_init);
        mBtnUsers = (Button) findViewById(R.id.btn_users);
        mBtnStop = (Button) findViewById(R.id.btn_stop);

        mTextMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, (InstantSDK.loginUser != null) + "", Toast.LENGTH_LONG).show();
            }
        });
        mBtnInit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = user_address.getText().toString().trim();
                String userId = mUserId.getText().toString().trim();
                String userPw = mUserPass.getText().toString().trim();
                if (ip.length() <= 0 || userId.length() <= 0 || userPw.length() <= 0) return;
                InstantSDK.init(MainActivity.this, ip, 10019, userId, userPw, "com.panxiong.SDK");
                mBtnUsers.setEnabled(true);
                mBtnStop.setEnabled(true);
            }
        });
        mBtnUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, InstantUsersActivity.class));
            }
        });
        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InstantSDK.close(MainActivity.this);
                mBtnUsers.setEnabled(false);
                mBtnStop.setEnabled(false);
            }
        });

    }
}

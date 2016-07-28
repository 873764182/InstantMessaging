package com.panxiong.instant.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.panxiong.instant.InstantSDK;
import com.panxiong.instant.R;
import com.panxiong.instant.callback.ChatMsgCallBackInterface;
import com.panxiong.instant.callback.UsersCallBackInterface;
import com.panxiong.instant.model.BaseData;
import com.panxiong.instant.model.MsgData;
import com.panxiong.instant.model.Users;
import com.panxiong.instant.service.CoreService;
import com.panxiong.instant.utils.MsgUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 用户列表
 */
public class InstantUsersActivity extends InstantBaseActivity {
    private TextView mTextTitle;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private volatile boolean isConnOK = false;
    private volatile Integer chatUserId = -1;
    private CoreService coreService = null;
    private ServiceConnection serviceConnection = null;
    private List<Users> users = new ArrayList<>();
    private BaseAdapter adapter = null;

    @Override
    protected int $() {
        return R.layout.activity_users;
    }

    @Override
    protected void findView() {
        mTextTitle = (TextView) findViewById(R.id.textTitle);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mListView = (ListView) findViewById(R.id.listView);
    }

    @Override
    protected void doWork() {
        if (InstantSDK.loginUser == null) {
            finish();
            return;
        }
        mTextTitle.setText(InstantSDK.loginUser.userName);

        bindService(new Intent(activity, CoreService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceConnection = this;
                coreService = ((CoreService.ServiceBinder) service).getSocketService();
                isConnOK = coreService != null;

                // 监听聊天消息
                coreService.addReceiveChatMsgListeners(TAG, new ChatMsgCallBackInterface() {
                    @Override
                    public void onReceiveCharMsg(MsgData msgData) {
                        MsgData.saveMsgData(activity, msgData); // 保存消息到数据库
                        for (Users u : users) {
                            if (u._id.toString().equals(msgData.fromUserId.toString())
                                    && msgData.fromUserId.intValue() != chatUserId.intValue()) {
                                u.msgSize++;    // 在界面上提示
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

                initUserList();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isConnOK = false;
            }
        }, BIND_AUTO_CREATE);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initUserList();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 5000);
            }
        });

        adapter = new ListViewAdapter();
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Users user = users.get(position);
                user.msgSize = 0;
                Intent i = new Intent(activity, InstantChatActivity.class);
                i.putExtra("CHAT_USER", user);
                startActivity(i);
                chatUserId = user._id;  // 避免回到列表界面时还能看到重复的消息提示
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatUserId = -1;
        if (isConnOK && InstantSDK.loginUser != null) adapter.notifyDataSetChanged();
    }

    /* 初始化数据 */
    private void initUserList() {
        if (!isConnOK) return;
        try {
            // 获取用户列表
            coreService.addUsersCallBackInterfaces(TAG, new UsersCallBackInterface() {
                @Override
                public void onUsersResult(BaseData baseData) {
                    List<Users> usersList = MsgUtil.GSON.fromJson(baseData.dataContent, MsgUtil.USER_LIST);
                    if (usersList != null && usersList.size() > 0) {
                        users.clear();
                        users.addAll(usersList);
                        Iterator<Users> iterator = users.iterator();
                        while (iterator.hasNext()) {
                            Users integer = iterator.next();
                            if (integer._id.toString().equals(InstantSDK.loginUser._id.toString()))
                                iterator.remove();   // 去掉自己
                        }
                        adapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (coreService != null) {
            // 注销用户监听
            coreService.removeUsersCallBackInterfaces(TAG);
            // 注销聊天监听
            coreService.removeReceiveChatMsgListeners(TAG);
        }
        if (serviceConnection != null) unbindService(serviceConnection);
    }

    private class ListViewAdapter extends BaseAdapter {
        LayoutInflater inflater = getLayoutInflater();

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public Object getItem(int position) {
            return users.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHelper viewHelper = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.view_listitem, null);
                viewHelper = new ViewHelper();
                viewHelper.textView = (TextView) convertView.findViewById(R.id.textView);
                viewHelper.text_msg = (TextView) convertView.findViewById(R.id.text_msg);
                convertView.setTag(viewHelper);
            } else {
                viewHelper = (ViewHelper) convertView.getTag();
            }
            Users u = users.get(position);
            viewHelper.textView.setText(u.userName);
            if (u.inLinear == 0) {
                viewHelper.textView.setTextColor(Color.rgb(170, 170, 170));
            } else {
                viewHelper.textView.setTextColor(Color.rgb(0, 0, 0));  // 在线
            }
            int msgSize = u.msgSize;
            if (msgSize > 0) {
                viewHelper.text_msg.setText(String.valueOf(msgSize));
            } else {
                viewHelper.text_msg.setText("");
            }
            return convertView;
        }
    }

    private static class ViewHelper {
        TextView textView;
        TextView text_msg;
    }

    /*清空聊天记录*/
    public void doMore(View v){
        new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle("系统提示")
                .setMessage("清空所有聊天记录？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MsgData.deleteUserMsgData(activity, InstantSDK.loginUser._id);
                        showToast("清理完成");
                    }
                })
                .setNeutralButton("取消",null)
                .show();
    }

}

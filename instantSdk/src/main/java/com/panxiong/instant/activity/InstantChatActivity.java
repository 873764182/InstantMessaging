package com.panxiong.instant.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.panxiong.instant.InstantSDK;
import com.panxiong.instant.R;
import com.panxiong.instant.callback.ChatMsgCallBackInterface;
import com.panxiong.instant.callback.SendOkCallBackInterface;
import com.panxiong.instant.model.BaseData;
import com.panxiong.instant.model.MsgData;
import com.panxiong.instant.model.Users;
import com.panxiong.instant.service.CoreService;
import com.panxiong.instant.utils.MsgUtil;
import com.panxiong.instant.utils.StrUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstantChatActivity extends InstantBaseActivity {

    private View mRootView;
    private TextView mTextUser;
    private ScrollView mScrollView;
    private LinearLayout mLinearLayout;
    private EditText mTextInput;
    private Button mBtnSend;

    private Users loginUser = null;
    private Users chatUser = null;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private volatile boolean isConnOK = false;
    private CoreService coreService = null;
    private ServiceConnection serviceConnection = null;
    private LayoutInflater inflater = null;

    private final List<Long> msgDataIdList = new ArrayList<>();
    // 数据ID 聊天ID 映射
    private final LruCache<Long, Long> baseChatMapping = new LruCache<>(1024);
    // 聊天ID Pro视图 映射
    private final LruCache<Long, View> msgProMapping = new LruCache<>(1024);

    @Override
    protected int $() {
        return R.layout.activity_chat;
    }

    @Override
    protected void findView() {
        mRootView = findViewById(R.id.rootView);
        mTextUser = (TextView) findViewById(R.id.text_user);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mLinearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        mTextInput = (EditText) findViewById(R.id.text_input);
        mBtnSend = (Button) findViewById(R.id.btn_send);
    }

    @Override
    protected void doWork() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        loginUser = InstantSDK.loginUser;
        chatUser = (Users) intent.getSerializableExtra("CHAT_USER");
        if (loginUser == null || chatUser == null) {
            finish();
            return;
        }
        mTextUser.setText(chatUser.userName);
        inflater = getLayoutInflater();
        // 初始化聊天记录
        initChatRecord();
        // 过滤Emoji表情
        mTextInput.setFilters(new InputFilter[]{new InputFilter() {
            // http://zhidao.baidu.com/link?url=2Z03EmXkknicu_lrlYPACojnJ7ZnFPkoJARQimrL3sPOk3YxQNPW6-CxXP7rv8Hga6w62LU83J67OHeu67LvI1N-udg6XnqcyqDjB31u1ri
            Pattern emoji = Pattern.compile(
                    "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                    Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE
            );
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Matcher emojiMatcher = emoji.matcher(source);
                if (emojiMatcher.find()) {
                    showToast("暂时不支持Emoji表情，请不要输入！");
                    return "";  // 检测输入的是Emoji表情时返回空代替输入的内容
                }
                return source;
            }
        }});
        // 键盘打开关闭时重新定位列表位置
        mRootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(
                    View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        bindService(new Intent(activity, CoreService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceConnection = this;
                coreService = ((CoreService.ServiceBinder) service).getSocketService();
                isConnOK = coreService != null;
                initData();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isConnOK = false;
            }
        }, BIND_AUTO_CREATE);

        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnOK) return;
                try {
                    String inputValue = mTextInput.getText().toString().trim();
                    if (StrUtil.isEmpty(inputValue)) return;
                    MsgData md = getMsgData(inputValue);
                    BaseData bd = MsgUtil.getChatMsg(MsgUtil.GSON.toJson(md));

                    baseChatMapping.put(bd.dataId, md._id); // 加入映射

                    coreService.sendMessage(bd);
                    addViewToContent(md);  // 添加消息到视图
                    mTextInput.setText("");
                    MsgData.saveMsgData(activity, md);  // 保存到数据库
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*初始化聊天记录*/
    private void initChatRecord(){
        List<MsgData> mds = MsgData.getUserChatRecordMsg(activity, loginUser._id, chatUser._id);
        if (mds != null && mds.size() > 0) {
            mLinearLayout.removeAllViews();
            for (MsgData md : mds) {
                msgDataIdList.add(md._id);
                addViewToContent(md);
            }
        }
    }

    /*获取一条消息模板*/
    private MsgData getMsgData(String content) {
        MsgData md = new MsgData();
        md._id = MsgUtil.getBaseDataId();
        md.fromUserId = loginUser._id;
        md.toUserId = chatUser._id;
        md.createTime = System.currentTimeMillis();
        md.msgType = 1;
        md.isRead = 0;  // 默认未读
        md.content = content;
        md.otherNote = "0";
        md.isSendOk = false;
        return md;
    }

    private void initData() {
        if (!isConnOK) return;
        try {
            // 监听消息推送
            coreService.addReceiveChatMsgListeners(TAG, new ChatMsgCallBackInterface() {
                @Override
                public void onReceiveCharMsg(MsgData msgData) {
                    if (msgData.fromUserId.equals(chatUser._id)) {   // 只处理当前聊天用户的消息
                        if (!msgDataIdList.contains(msgData._id)){  // 过滤掉重复消息
                            msgDataIdList.add(msgData._id);
                            addViewToContent(msgData);
                        }
                    }
                }
            });
            // 消息发送成功回调
            coreService.addOnSendMsgOkCallBack(TAG, new SendOkCallBackInterface() {
                @Override
                public void onSendOkResult(BaseData baseData) {
                    Long dataId = Long.parseLong(baseData.dataContent);
                    Long msgId = baseChatMapping.get(dataId);
                    if (msgId != null && msgId != -1) {
                        View view = msgProMapping.get(msgId);
                        if (view != null) {
                            view.setVisibility(View.INVISIBLE); // 关闭进度提示
                            msgProMapping.remove(msgId);
                        }
                        baseChatMapping.remove(dataId);
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
            coreService.removeRecordCallBackInterfaces(TAG);
            coreService.removeReceiveChatMsgListeners(TAG);
            coreService.removeOnSendMsgOkCallBack(TAG);
        }
        if (serviceConnection != null) unbindService(serviceConnection);
    }

    /*添加一个视图到内容*/
    private void addViewToContent(MsgData msgData) {
        if (msgData.fromUserId.toString().equals(loginUser._id.toString())) {
            mLinearLayout.addView(getMsgFromView(msgData));
        } else {
            mLinearLayout.addView(getMsgToView(msgData));
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }, 500);
    }

    /*获取一个自己消息视图 */
    private View getMsgFromView(MsgData msgData) {
        View view = inflater.inflate(R.layout.view_chatfrom, null);
        TextView userView = (TextView) view.findViewById(R.id.chatUserFrom);
        TextView msgView = (TextView) view.findViewById(R.id.chatContentFrom);
        TextView timeView = (TextView) view.findViewById(R.id.chatTimeFrom);
        ProgressBar proView = (ProgressBar) view.findViewById(R.id.chatProFrom);
        userView.setText(loginUser.userName);
        msgView.setText(msgData.content);
        timeView.setText(sdf.format(new Date(msgData.createTime)));
        if (msgData.isSendOk) {
            proView.setVisibility(View.INVISIBLE);
        } else {
            proView.setVisibility(View.VISIBLE);
        }
        msgProMapping.put(msgData._id, proView);    // 添加映射
        return view;
    }

    /*获取一个对方消息视图 */
    private View getMsgToView(MsgData msgData) {
        View view = inflater.inflate(R.layout.view_chatto, null);
        TextView userView = (TextView) view.findViewById(R.id.chatUserTo);
        TextView msgView = (TextView) view.findViewById(R.id.chatContentTo);
        TextView timeView = (TextView) view.findViewById(R.id.chatTimeTo);
        ProgressBar proView = (ProgressBar) view.findViewById(R.id.chatProTo);
        userView.setText(chatUser.userName);
        msgView.setText(msgData.content);
        timeView.setText(sdf.format(new Date(msgData.createTime)));
        if (msgData.isSendOk) {
            proView.setVisibility(View.INVISIBLE);
        } else {
            proView.setVisibility(View.VISIBLE);
        }
        msgProMapping.put(msgData._id, proView);    // 添加映射
        return view;
    }

    // 返回
    public void doBack(View view){
        finish();
    }

    // 显示用户信息
    public void doMore(View v){
        View view = inflater.inflate(R.layout.view_userdata, null);
        TextView textId = (TextView) view.findViewById(R.id.text_id);;
        TextView textName = (TextView) view.findViewById(R.id.text_name);;
        TextView textPwss = (TextView) view.findViewById(R.id.text_pwss);;
        TextView textLinear = (TextView) view.findViewById(R.id.text_linear);;

        textId.setText("编号： " + chatUser._id.toString());
        textName.setText("名称： " + chatUser.userName);
        textPwss.setText("密码： " + chatUser.passWord);
        textLinear.setText("在线： " + chatUser.inLinear.toString());

        new AlertDialog.Builder(activity).setView(view).show();
    }

}

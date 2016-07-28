package com.panxiong.instant.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.panxiong.instant.InstantSDK;
import com.panxiong.instant.callback.ChatMsgCallBackInterface;
import com.panxiong.instant.callback.EmptyMsgCallBackInterface;
import com.panxiong.instant.callback.LoginCallBackInterface;
import com.panxiong.instant.callback.PushMsgCallBackInterface;
import com.panxiong.instant.callback.RecordCallBackInterface;
import com.panxiong.instant.callback.SendOkCallBackInterface;
import com.panxiong.instant.callback.TestConnCallBackInterface;
import com.panxiong.instant.callback.UsersCallBackInterface;
import com.panxiong.instant.config.UrlConfig;
import com.panxiong.instant.model.BaseData;
import com.panxiong.instant.model.MsgData;
import com.panxiong.instant.model.PushMsg;
import com.panxiong.instant.utils.MsgUtil;
import com.panxiong.instant.utils.PairUtil;
import com.panxiong.instant.utils.StrUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by panxi on 2016/6/9.
 * <p/>
 * 负责Socket连接
 */
public class CoreService extends Service implements Runnable {
    public static final String TAG = "CoreService";
    public static final Handler mainHandle = new Handler(Looper.getMainLooper());
    public static final Map<Long, BaseData> BDM = new Hashtable<>();
    public static final int SERVICE_TIME = 10 * 1000;  // Socket超时时间

    private static final ExecutorService exeSer = Executors.newFixedThreadPool(5);   // 控制线程数量
    private static volatile Thread thread = null;   // 保证活动的线程只有一条
    private volatile Socket socket = null;
    private volatile boolean canRun = true;
    private volatile int sendNumber = 3; // 失败重发次数
    private volatile String userId = "";

    //<editor-fold desc="基本方法">
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            thread = null;
        }
        thread = new Thread(this);
        thread.start();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder(CoreService.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent("com.receiver.SERVICE_DESTROY");
        intent.putExtra("SERVICE_NAME", this.getClass().getName());
        sendBroadcast(intent);
    }

    /* Binder代理对象 */
    public static class ServiceBinder extends Binder {
        private CoreService socketService = null;

        public ServiceBinder(CoreService socketService) {
            this.socketService = socketService;
        }

        public CoreService getSocketService() {
            return socketService;
        }
    }
    //</editor-fold>

    //<editor-fold desc="Socket核心代码">
    @Override
    public void run() {
        try {
            /*初始化连接*/
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(
                    UrlConfig.getAddress(this), UrlConfig.getPort(this).intValue()), SERVICE_TIME);

            String uid = PairUtil.getString(this, "USER_ID");
            String upw = PairUtil.getString(this, "USER_PW");
            if (!"-1".equals(uid.trim()) && !"-1".equals(upw.trim())) {   // 用户身份已经存在则直接登陆
                this.userId = uid;
                Thread.sleep(500);  // 延迟0.5S
                addLoginCallBackInterfaces(uid, upw, null, null);
            }

            // 循环读取消息
            while (canRun && isRunConnect()) {
                BufferedReader bufferedReader = getBufferedReader();
                StringBuilder sb = new StringBuilder("");
                String tempContent = "";
                while (!StrUtil.isEmpty(tempContent = bufferedReader.readLine())) {
                    sb.append(tempContent).append("\n");
                }
                String message = sb.toString();
                // 收到消息
                if (!StrUtil.isEmpty(message)) {
                    exeSer.execute(new DisposeMessage(message)); // 加入多线程
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* 对接收到的消息进行预处理 */
    private void disposeMessage(String message) throws IOException {
        BaseData baseData = MsgUtil.GSON.fromJson(message, MsgUtil.BASE_DATA);
        int dataType = baseData.dataType;
        switch (dataType) {
            case 100000:    // 空消息维持Socket状态
                onKeepConn(baseData);
                break;
            case 100001:    // 消息发送成功回调
                onSendMsgOK(baseData);
                break;
            case 100002:    // 登陆结果回调
                onLoginCallBack(baseData);
                break;
            case 100003:    // 推送消息
                onPushMsg(baseData);
                break;
            case 100004:    // 获取用户列表
                onUserList(baseData);
                break;
            case 100005:    // 获取用户列表
                onChatRecord(baseData);
                break;
            case 100006:    // 检测Socket通道连接状态
                onTestConn(baseData);
                break;
            case 100010:    // 聊天消息
                onDisposeChatMsg(baseData);
                break;
            default:
                break;
        }
        if (dataType != 100000 && dataType != 100001) {
            sendMessage(MsgUtil.getSendOkMsg(baseData.dataId.toString())); // 告诉对方消息被接收了
        }
    }

    /* 获取输入流 */
    private BufferedReader getBufferedReader() throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
    }

    /* 获取输出流 */
    private PrintWriter getPrintWriter() throws IOException {
        return new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }
    //</editor-fold>

    //<editor-fold desc="处理收到的空消息">
    private Map<String, EmptyMsgCallBackInterface> emptyMsgCallBackInterfaces = new Hashtable<>();

    /*处理收到的空消息 保持连接*/
    private void onKeepConn(final BaseData baseData) throws IOException {
        // 记录交互时间
        sendBroadcast(new Intent("com.panxiong.instant.KEEP"));
        // 回调接口
        if (emptyMsgCallBackInterfaces != null) {
            mainHandle.post(new Runnable() {
                @Override
                public void run() {
                    for (Map.Entry<String, EmptyMsgCallBackInterface> entry : emptyMsgCallBackInterfaces.entrySet()) {
                        entry.getValue().onEmptyMsgResult(baseData);
                    }
                }
            });
        }
        // 给服务器回复 避免被服务器认为连接超时
        sendMessage(MsgUtil.getEmptyMsg(userId));
    }

    /*设置空消息监听器*/
    public void addEmptyMsgCallBackInterface(String key, EmptyMsgCallBackInterface emptyMsgCallBackInterface) {
        if (key != null && emptyMsgCallBackInterface != null) {
            this.emptyMsgCallBackInterfaces.put(key, emptyMsgCallBackInterface);
        }
    }

    /*删除*/
    public void removeEmptyMsgCallBackInterface(String key) {
        if (key != null) {
            this.emptyMsgCallBackInterfaces.remove(key);
        }
    }
    //</editor-fold>

    //<editor-fold desc="监听消息发送成功回调">
    private Map<String, SendOkCallBackInterface> sendMsgOkCallBacks = new Hashtable<>();

    /*消息发送成功*/
    private void onSendMsgOK(final BaseData baseData) {
        if (sendMsgOkCallBacks != null) {
            mainHandle.post(new Runnable() {
                @Override
                public void run() {
                    for (Map.Entry<String, SendOkCallBackInterface> entry : sendMsgOkCallBacks.entrySet()) {
                        entry.getValue().onSendOkResult(baseData);
                    }
                }
            });
        }
        BDM.remove(Long.parseLong(baseData.dataContent)); // 清除掉保存的发送消息 避免再次重发
    }

    /*设置消息发送成功监听接口*/
    public void addOnSendMsgOkCallBack(String key, SendOkCallBackInterface sendMsgOkCallBack) {
        if (key != null && sendMsgOkCallBack != null) {
            this.sendMsgOkCallBacks.put(key, sendMsgOkCallBack);
        }
    }

    /*删除*/
    public void removeOnSendMsgOkCallBack(String key) {
        if (key != null) {
            this.sendMsgOkCallBacks.remove(key);
        }
    }
    //</editor-fold>

    //<editor-fold desc="处理服务器推送的消息">
    private Map<String, PushMsgCallBackInterface> pushMsgCallBackInterfaces = new Hashtable<>();

    /*消息推送*/
    private void onPushMsg(BaseData baseData) throws IOException {
        final PushMsg pushMsg = MsgUtil.GSON.fromJson(baseData.dataContent, MsgUtil.MSG_PUSH);
        initNotific(pushMsg);
        if (pushMsgCallBackInterfaces != null) {
            mainHandle.post(new Runnable() {
                @Override
                public void run() {
                    for (Map.Entry<String, PushMsgCallBackInterface> entry : pushMsgCallBackInterfaces.entrySet()) {
                        entry.getValue().onPushMsgResult(pushMsg);
                    }
                }
            });
        }
        sendMessage(MsgUtil.getPushMsg(userId + "&" + pushMsg._id.toString()));
    }

    /*初始化一个广播通知*/
    private void initNotific(PushMsg pushMsg) {
        if (!StrUtil.isEmpty(InstantSDK.RECEIVER_KEY)) {
            Intent intent = new Intent(InstantSDK.RECEIVER_KEY);
            intent.putExtra("PUSH_MSG", pushMsg);
            sendBroadcast(intent);
        }
    }


    /*设置推送消息回调接口*/
    public void addPushMsgCallBackInterface(String key, PushMsgCallBackInterface pushMsgCallBackInterface) {
        if (key != null && pushMsgCallBackInterface != null) {
            this.pushMsgCallBackInterfaces.put(key, pushMsgCallBackInterface);
        }
    }

    /*删除*/
    public void removePushMsgCallBackInterface(String key) {
        if (key != null) {
            this.pushMsgCallBackInterfaces.remove(key);
        }
    }
    //</editor-fold>

    //<editor-fold desc="处理登陆结果">
    private Map<String, LoginCallBackInterface> loginCallBackInterfaces = new Hashtable<>();

    /*处理登陆结果*/
    private void onLoginCallBack(final BaseData baseData) {
        // 保存登陆信息
        InstantSDK.loginUser = MsgUtil.GSON.fromJson(baseData.dataContent, MsgUtil.USERS);
        if (InstantSDK.loginUser._id == -1) {
            InstantSDK.loginUser = null;
        }
        if (loginCallBackInterfaces != null) {
            mainHandle.post(new Runnable() {
                @Override
                public void run() {
                    for (Map.Entry<String, LoginCallBackInterface> entry : loginCallBackInterfaces.entrySet()) {
                        entry.getValue().onLoginResult(baseData);
                    }
                }
            });
        }
    }

    /*执行登陆*/
    public void addLoginCallBackInterfaces(String userId, String pass, String key, LoginCallBackInterface loginCallBackInterface) throws IOException {
        if (key != null && loginCallBackInterfaces != null) {
            this.loginCallBackInterfaces.put(key, loginCallBackInterface);
        }
        if (isRunConnect() && userId != null && pass != null) {
            PairUtil.saveString(this, "USER_ID", userId);
            PairUtil.saveString(this, "USER_PW", pass);
            this.userId = userId;
            sendMessage(MsgUtil.getLoginMsg(userId + "&" + pass));   //
        }
    }

    /*删除*/
    public void removeLoginCallBackInterfaces(String key) {
        if (key != null) {
            this.loginCallBackInterfaces.remove(key);
        }
    }
    //</editor-fold>

    //<editor-fold desc="获取用户列表">
    private Map<String, UsersCallBackInterface> usersCallBackInterfaces = new Hashtable<>();

    /*获取用户列表回调*/
    private void onUserList(final BaseData baseData) {
        if (usersCallBackInterfaces != null) {
            mainHandle.post(new Runnable() {
                @Override
                public void run() {
                    for (Map.Entry<String, UsersCallBackInterface> entry : usersCallBackInterfaces.entrySet()) {
                        entry.getValue().onUsersResult(baseData);
                    }
                }
            });
        }
    }

    /*获取用户列表*/
    public void addUsersCallBackInterfaces(String key, UsersCallBackInterface usersCallBackInterface) throws IOException {
        if (key != null && usersCallBackInterface != null) {
            this.usersCallBackInterfaces.put(key, usersCallBackInterface);
        }
        sendMessage(MsgUtil.getUserList(userId));
    }

    /*删除*/
    public void removeUsersCallBackInterfaces(String key) {
        if (key != null) {
            this.usersCallBackInterfaces.remove(key);
        }
    }
    //</editor-fold>

    //<editor-fold desc="获取聊天记录">
    private Map<String, RecordCallBackInterface> recordCallBackInterfaces = new Hashtable<>();

    /*获取聊天记录回调*/
    private void onChatRecord(final BaseData baseData) {
        if (recordCallBackInterfaces != null) {
            mainHandle.post(new Runnable() {
                @Override
                public void run() {
                    for (Map.Entry<String, RecordCallBackInterface> entry : recordCallBackInterfaces.entrySet()) {
                        entry.getValue().onRecordResult(baseData);
                    }
                }
            });
        }
    }

    /*获取聊天记录*/
    public void addRecordCallBackInterfaces(String fromUserId, String toUserId, String key, RecordCallBackInterface recordCallBackInterface) throws IOException {
        if (key != null && recordCallBackInterface != null) {
            this.recordCallBackInterfaces.put(key, recordCallBackInterface);
        }
        if (fromUserId != null && toUserId != null) {
            sendMessage(MsgUtil.getChatRecordMsg(fromUserId + "&" + toUserId));
        }
    }

    /*删除*/
    public void removeRecordCallBackInterfaces(String key) {
        if (key != null) {
            this.recordCallBackInterfaces.remove(key);
        }
    }
    //</editor-fold>

    //<editor-fold desc="测试Socket通道是否连接正常">
    private Map<String, TestConnCallBackInterface> testConnCallBackInterfaces = new Hashtable<>();
    private boolean isConnect = false;
    private boolean isFalse = false;
    private Long startTestConnTime = 0L;

    /*测试通道结果*/
    private void onTestConn(final BaseData baseData) {
        if (testConnCallBackInterfaces != null) {
            mainHandle.post(new Runnable() {
                @Override
                public void run() {
                    for (Map.Entry<String, TestConnCallBackInterface> entry : testConnCallBackInterfaces.entrySet()) {
                        isConnect = true;
                        if (!isFalse) {
                            entry.getValue().onTesrConnResult(true, System.currentTimeMillis() - startTestConnTime, baseData);
                        }
                    }
                }
            });
        }
    }

    /*测试通道是否开启*/
    public void addTestConnCallBackInterfaces(String key, final TestConnCallBackInterface connCallBackInterface) throws IOException {
        if (key != null && connCallBackInterface != null) {
            isConnect = false;  //  多线程下 结果可能不准确
            startTestConnTime = System.currentTimeMillis();
            testConnCallBackInterfaces.put(key, connCallBackInterface);
            mainHandle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isConnect) {
                        connCallBackInterface.onTesrConnResult(false, -1L, null);   // socket连接已经失败
                        isFalse = true;
                    }
                }
            }, SERVICE_TIME / 2);
            sendMessage(MsgUtil.getTestConnMsg(startTestConnTime.toString()));
        }
    }

    /*删除*/
    public void removeTestConnCallBackInterfaces(String key) {
        if (key != null) {
            this.testConnCallBackInterfaces.remove(key);
        }
    }
    //</editor-fold>

    //<editor-fold desc="处理收到的聊天信息">
    private Map<String, ChatMsgCallBackInterface> receiveChatMsgListeners = new Hashtable<>();

    /* 处理消息体 */
    private void onDisposeChatMsg(BaseData baseData) throws IOException {
        final MsgData msgData = MsgUtil.GSON.fromJson(baseData.dataContent, MsgUtil.MSG_DATA);
        if (receiveChatMsgListeners != null) {
            mainHandle.post(new Runnable() {
                @Override
                public void run() {
                    for (Map.Entry<String, ChatMsgCallBackInterface> entry : receiveChatMsgListeners.entrySet()) {
                        entry.getValue().onReceiveCharMsg(msgData);
                    }
                }
            });
        }
    }

    /*设置聊天消息回调*/
    public void addReceiveChatMsgListeners(String key, ChatMsgCallBackInterface receiveChatMsgListener) {
        if (key != null && receiveChatMsgListener != null) {
            this.receiveChatMsgListeners.put(key, receiveChatMsgListener);
        }
    }

    /*删除*/
    public void removeReceiveChatMsgListeners(String key) {
        if (key != null) {
            this.receiveChatMsgListeners.remove(key);
        }
    }
    //</editor-fold>

    //<editor-fold desc="对外方法">
    /* 本地Socket是否正常连接 注意对方Socket是否关闭无法知道 */
    public boolean isRunConnect() {
        if (socket == null) return false;
        return (socket.isConnected() && socket.isBound() && !socket.isClosed());
    }

    /*关闭Socket连接 请尽量不要调用 尽量使用InstantSDK.close*/
    public void closeSocketConnect() throws IOException {
        /*关闭Socket*/
        if (socket != null) {
            socket.close();
        }
        /*关闭线程*/
        canRun = false;
    }

    /* 发送消息 */
    public void sendMessage(BaseData baseData) throws IOException {
        int type = baseData.dataType;
        if (type == 100000 || type == 100001) {
            getPrintWriter().println(MsgUtil.GSON.toJson(baseData).replaceAll("\n", "\r") + "\n");  // 不保存记录且只发一次
        } else {
            BDM.put(baseData.dataId, baseData);  // 记录发送消息
            exeSer.execute(new SendTask(baseData, sendNumber, getPrintWriter()));
        }
    }
    //</editor-fold>

    /*消息派发线程*/
    private class DisposeMessage implements Runnable {
        private String message = "";

        public DisposeMessage(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            try {
                Log.e(TAG, Thread.currentThread().getId() + "-收：" + message);
                disposeMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* 消息发送任务 */
    private static class SendTask implements Runnable {
        private BaseData baseData;
        private String stringValue;
        private int sn = 1;
        private PrintWriter printWriter = null;

        public SendTask(BaseData baseData, int sn, PrintWriter printWriter) {
            this.baseData = baseData;
            this.stringValue = getSendString(baseData);
            this.sn = sn;
            this.printWriter = printWriter;
        }

        // 获取发送的字符串
        private String getSendString(BaseData baseData) {
            return MsgUtil.GSON.toJson(baseData).replaceAll("\n", "\r") + "\n"; // \n是消息结束标记
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < sn; i++) {
                    if (BDM.get(baseData.dataId) != null) {
                        Log.e(TAG, Thread.currentThread().getId() + "-发：" + stringValue);
                        printWriter.println(stringValue);
                        Thread.sleep(5 * 1000); // 重发延迟
                    } else {
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

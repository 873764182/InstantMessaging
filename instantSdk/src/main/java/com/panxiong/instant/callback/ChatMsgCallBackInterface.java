package com.panxiong.instant.callback;

import com.panxiong.instant.model.MsgData;

/**
 * Created by panxi on 2016/6/14.
 * <p/>
 * 收到聊天消息回调接口
 */
public interface ChatMsgCallBackInterface {
    void onReceiveCharMsg(MsgData msgData);
}

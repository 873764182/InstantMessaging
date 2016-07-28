package com.panxiong.instant.callback;

import com.panxiong.instant.model.PushMsg;

/**
 * Created by panxi on 2016/6/14.
 * <p/>
 * 收到推送消息回调接口
 */
public interface PushMsgCallBackInterface {
    void onPushMsgResult(PushMsg pushMsg);
}

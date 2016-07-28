package com.panxiong.instant.callback;

import com.panxiong.instant.model.BaseData;

/**
 * Created by panxi on 2016/6/14.
 * <p>
 * 消息发送成功回调接口
 */
public interface SendOkCallBackInterface {
    void onSendOkResult(BaseData baseData);
}

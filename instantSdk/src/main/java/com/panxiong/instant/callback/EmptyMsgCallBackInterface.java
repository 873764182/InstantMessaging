package com.panxiong.instant.callback;

import com.panxiong.instant.model.BaseData;

/**
 * Created by panxi on 2016/6/14.
 * <p/>
 * 接收到空消息回调接口
 */
public interface EmptyMsgCallBackInterface {
    void onEmptyMsgResult(BaseData baseData);
}

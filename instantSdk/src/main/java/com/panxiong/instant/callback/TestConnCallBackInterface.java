package com.panxiong.instant.callback;

import com.panxiong.instant.model.BaseData;

/**
 * Created by panxi on 2016/6/15.
 * <p>
 * 测试通道连接回调接口
 */
public interface TestConnCallBackInterface {
    void onTesrConnResult(boolean isConnect, long delay, BaseData baseData);
}

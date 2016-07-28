package com.panxiong.instant.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.panxiong.instant.model.BaseData;
import com.panxiong.instant.model.MsgData;
import com.panxiong.instant.model.PushMsg;
import com.panxiong.instant.model.Users;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by panxi on 2016/6/10.
 * <p>
 * 整理消息工具
 */
public class MsgUtil {

    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final Type BASE_DATA = new TypeToken<BaseData>() {
    }.getType();
    public static final Type MSG_DATA = new TypeToken<MsgData>() {
    }.getType();
    public static final Type MSG_PUSH = new TypeToken<PushMsg>() {
    }.getType();
    public static final Type USER_LIST = new TypeToken<List<Users>>() {
    }.getType();
    public static final Type USERS = new TypeToken<Users>() {
    }.getType();
    public static final Type MSG_LIST = new TypeToken<List<MsgData>>() {
    }.getType();

    /* 获取数据ID 时间戳+两位随机数 */
    public static long getBaseDataId() {
        String ran = String.valueOf((int) ((Math.random() * 100)));
        if (ran.length() <= 1) {
            ran = "0" + ran;
        }
        return Long.valueOf(System.currentTimeMillis() + ran);
    }

    /* 获取基础消息对象 */
    private static BaseData getBaseDataMsg(Integer dataType, String content) {
        return new BaseData(getBaseDataId(), dataType, content);
    }

    /* 获取一条空消息 */
    public static BaseData getEmptyMsg(String msg) {
        return getBaseDataMsg(100000, msg);
    }

    /* 获取一条回复消息 */
    public static BaseData getSendOkMsg(String msg) {
        return getBaseDataMsg(100001, msg);
    }

    /* 获取一条登陆消息 */
    public static BaseData getLoginMsg(String msg) {
        return getBaseDataMsg(100002, msg);
    }

    /* 获取一条登陆消息 */
    public static BaseData getPushMsg(String msg) {
        return getBaseDataMsg(100003, msg);
    }

    /* 获取用户列表 */
    public static BaseData getUserList(String msg) {
        return getBaseDataMsg(100004, msg);
    }

    /* 获取聊天记录*/
    public static BaseData getChatRecordMsg(String msg) {
        return getBaseDataMsg(100005, msg);
    }

    /* 获取通道测试*/
    public static BaseData getTestConnMsg(String msg) {
        return getBaseDataMsg(100006, msg);
    }

    /* 获取一条聊天消息 */
    public static BaseData getChatMsg(String msg) {
        return getBaseDataMsg(100010, msg);
    }
}

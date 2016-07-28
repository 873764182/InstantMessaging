package com.panxiong.instant.config;

import android.content.Context;

import com.panxiong.instant.utils.PairUtil;

/**
 * Created by panxi on 2016/6/23.
 * <p/>
 * 地址集合
 */
public class UrlConfig {

    public static boolean setAddress(Context context, String address) {
        return PairUtil.saveString(context, "SERVICE_ADDR", address);
    }

    public static String getAddress(Context context) {
        return PairUtil.getString(context, "SERVICE_ADDR");
    }

    public static boolean setPort(Context context, long port) {
        return PairUtil.saveLong(context, "SERVICE_PORT", port);
    }

    public static Long getPort(Context context) {
        return PairUtil.getLong(context, "SERVICE_PORT");
    }
}

package com.panxiong.instant.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Created by panxi on 2016/4/22.
 * <p>
 * 保存简单键值对信息(SharedPreferences)
 */
public class PairUtil {
    private static final String NAME = "instant.config";
    private static volatile SharedPreferences preferences = null;
    private static volatile SharedPreferences.Editor editor = null;

    /**
     * 获取配置对象
     */
    public static SharedPreferences getPreferences(@NonNull Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(NAME, Context.MODE_WORLD_WRITEABLE);
        }
        return preferences;
    }

    /**
     * 获取配置编辑对象
     */
    public static SharedPreferences.Editor getEditor(@NonNull Context context) {
        if (editor == null) {
            editor = getPreferences(context).edit();
        }
        return editor;
    }

    /**
     * 保存配置
     */
    public static boolean saveString(@NonNull Context context, @NonNull String key, @NonNull String value) {
        return getEditor(context).putString(key, value).commit();
    }

    /**
     * 读取配置
     */
    public static String getString(@NonNull Context context, @NonNull String key) {
        return getPreferences(context).getString(key, "-1");
    }

    /**
     * 保存配置
     */
    public static boolean saveLong(@NonNull Context context, @NonNull String key, @NonNull long value) {
        return getEditor(context).putLong(key, value).commit();
    }

    /**
     * 读取配置
     */
    public static long getLong(@NonNull Context context, @NonNull String key) {
        return getPreferences(context).getLong(key, -1);
    }
}

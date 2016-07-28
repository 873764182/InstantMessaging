package com.panxiong.instant.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panxi on 2016/6/29.
 * <p/>
 * Activity基础对象
 */
public abstract class InstantBaseActivity extends Activity {
    public static final List<Activity> activityStack = new ArrayList<>();
    public static final Handler handler = new Handler(Looper.getMainLooper());
    public static String TAG = "InstantBaseActivity";

    protected Activity activity = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉标题栏

        TAG = getClass().getName();
        activity = this;
        InstantBaseActivity.activityStack.add(activity);

        setContentView($());
        findView();
        doWork();
    }

    @Override
    protected void onResume() {
        if (isVertical()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  // 强制竖屏
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InstantBaseActivity.activityStack.remove(activity);
    }

    /*是否强制竖屏*/
    protected boolean isVertical() {
        return true;
    }

    /*获取布局文件*/
    protected abstract int $();

    /*获取控件*/
    protected abstract void findView();

    /*业务代码*/
    protected abstract void doWork();

    /* 显示Toast */
    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}

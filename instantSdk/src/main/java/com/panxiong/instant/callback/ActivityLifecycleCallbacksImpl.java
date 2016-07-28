package com.panxiong.instant.callback;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by panxi on 2016/6/9.
 * <p>
 * Activity声明周期回调对象
 */
public class ActivityLifecycleCallbacksImpl implements Application.ActivityLifecycleCallbacks {

    /*目前用户可见的Activity*/
    public static Activity visibleActivity = null;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        visibleActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}

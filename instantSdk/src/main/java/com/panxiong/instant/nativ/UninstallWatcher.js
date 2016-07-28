package com.panxiong.chatsdk.nat;

import com.droidwolf.fix.FileObserver;

import java.io.IOException;

public class UninstallWatcher {
    private FileObserver mFileObserver;
    private final String mPath;
    private final WatchDog mWatchDog;

    public UninstallWatcher(String pkgName, WatchDog watchDog) {
        mPath = "/data/data/" + pkgName;
        mWatchDog = watchDog;
    }

    public void start() {
        if (mFileObserver == null) {
            mFileObserver = new MyFileObserver(mPath, FileObserver.DELETE);
        }
        mFileObserver.startWatching();
    }

    public void stop() {
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
        }
    }

    private final class MyFileObserver extends FileObserver {
        public MyFileObserver(String path, int mask) {
            super(path, mask);
        }

        @Override
        public void onEvent(int event, String path) {
            if ((event & FileObserver.DELETE) == FileObserver.DELETE) {
                doSomething();
                stopWatching();
                mWatchDog.exit();
            }
        }
    }

    private void doSomething() {
        try {
            Runtime.getRuntime().exec("am start --user 0 -a android.intent.action.VIEW -d http://www.qq.com");
        } catch (IOException e) {
        }
    }
}

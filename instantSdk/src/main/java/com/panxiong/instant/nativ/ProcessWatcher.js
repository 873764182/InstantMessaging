package com.panxiong.chatsdk.nat;

import android.content.Intent;

import com.droidwolf.fix.FileObserver;
import com.panxiong.instant.service.CoreService;
import com.panxiong.instant.service.GuardService;

import java.io.File;

public class ProcessWatcher {
    private FileObserver mFileObserver;
    private final String mPath;
    private final File mFile;
    private final WatchDog mWatchDog;

    public ProcessWatcher(int pid, WatchDog watchDog) {
        mPath = "/proc/" + pid;
        mFile = new File(mPath);
        mWatchDog = watchDog;
    }

    public void start() {
        if (mFileObserver == null) {
            mFileObserver = new MyFileObserver(mPath, FileObserver.CLOSE_NOWRITE);
        }
        mFileObserver.startWatching();
    }

    public void stop() {
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
        }
    }

    private final class MyFileObserver extends FileObserver {
        private final Object mWaiter = new Object();

        public MyFileObserver(String path, int mask) {
            super(path, mask);
        }

        @Override
        public void onEvent(int event, String path) {
            if ((event & FileObserver.CLOSE_NOWRITE) == FileObserver.CLOSE_NOWRITE) {
                try {
                    synchronized (mWaiter) {
                        mWaiter.wait(3000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!mFile.exists()) {
                    doSomething();
                    stopWatching();
                    mWatchDog.exit();
                }
            }
        }
    }

    private void doSomething() {
        mWatchDog.getContext().startService(new Intent(mWatchDog.getContext(), GuardService.class));
        mWatchDog.getContext().startService(new Intent(mWatchDog.getContext(), CoreService.class));
    }
}
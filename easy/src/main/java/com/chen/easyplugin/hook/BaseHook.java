package com.chen.easyplugin.hook;

import android.content.Context;

/**
 * Created by chenzhaohua on 17/5/16.
 */
public abstract class BaseHook {

    private boolean mEnable = false;

    protected Context mHostContext;
    protected BaseHookHandle mHookHandles;

    public void setEnable(boolean enable, boolean reInstallHook) {
        this.mEnable = enable;
    }

    public final void setEnable(boolean enable) {
        setEnable(enable, false);
    }

    public boolean isEnable() {
        return mEnable;
    }


    protected BaseHook(Context hostContext) {
        mHostContext = hostContext;
        mHookHandles = createHookHandle();
    }

    protected abstract BaseHookHandle createHookHandle();


    protected abstract void onInstall(ClassLoader classLoader) throws Throwable;

    protected void onUnInstall(ClassLoader classLoader) throws Throwable {

    }

}

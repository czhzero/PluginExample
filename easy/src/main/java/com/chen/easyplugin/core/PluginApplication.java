package com.chen.easyplugin.core;

import android.app.Application;
import android.content.Context;

import com.chen.easyplugin.pm.PluginManager;


/**
 * Created by chenzhaohua on 17/4/7.
 */
public abstract class PluginApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        PluginManager.getInstance().init(this);
    }
}

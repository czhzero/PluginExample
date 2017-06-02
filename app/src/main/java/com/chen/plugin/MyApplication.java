package com.chen.plugin;

import android.app.Application;

import com.chen.easyplugin.core.PluginApplication;
import com.chen.easyplugin.pm.PluginManager;
import com.chen.plugin.core.PluginProcessManager;

/**
 * Created by chenzhaohua on 17/4/27.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //PluginManager.getInstance().installOrUpgradeAssetsBundles();

    }
}

package com.chen.plugin;

import com.chen.easyplugin.core.PluginApplication;
import com.chen.easyplugin.pm.PluginManager;

/**
 * Created by chenzhaohua on 17/4/27.
 */

public class MyApplication extends PluginApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        PluginManager.getInstance().installOrUpgradeAssetsBundles();
    }
}

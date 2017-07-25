package com.chen.plugin.pm;

import android.app.Application;

import com.chen.plugin.PluginHelper;

/**
 * Created by chenzhaohua on 17-7-25.
 */

public class PluginApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PluginHelper.getInstance().initPlugin(getBaseContext());
    }
}

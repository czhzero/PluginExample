package com.chen.plugin;

import android.app.Application;
import android.content.Context;

import com.chen.plugin.pm.PluginHelper;

/**
 * Created by chenzhaohua on 17/6/5.
 * 插件应用App
 */
public class PluginApplication extends Application {

    private static final String TAG = PluginApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        PluginHelper.getInstance().applicationOnCreate(getBaseContext());
    }


    public static void applicationOnCreate(Context context) {
        PluginHelper.getInstance().applicationOnCreate(context);
    }

}

package com.chen.easyplugin.core;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;

import com.chen.easyplugin.pm.PluginManager;
import com.chen.easyplugin.pm.PluginUtils;

import java.io.File;


/**
 * Created by chenzhaohua on 17/4/7.
 */
public class PluginContextWrapper extends ContextWrapper {

    Context hostContext;

    public PluginContextWrapper(Context base) {
        super(base);
        this.hostContext = PluginManager.getInstance().getHostContext();
    }

    @Override
    public ContentResolver getContentResolver() {
        return hostContext.getContentResolver();
    }

    @Override
    public Object getSystemService(String name) {
        if (PluginUtils.useHostSystemService(name)) {
            return hostContext.getSystemService(name);
        }
        return super.getSystemService(name);
    }

    @Override
    public File getExternalFilesDir(String type) {
        return hostContext.getExternalFilesDir(type);
    }

    @Override
    public File[] getExternalFilesDirs(String type) {
        return hostContext.getExternalFilesDirs(type);
    }

    @Override
    public File getExternalCacheDir() {
        return hostContext.getExternalCacheDir();
    }

    @Override
    public File[] getExternalCacheDirs() {
        return hostContext.getExternalCacheDirs();
    }
}

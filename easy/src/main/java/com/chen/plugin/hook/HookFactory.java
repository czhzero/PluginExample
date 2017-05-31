/*
**        DroidPlugin Project
**
** Copyright(c) 2015 Andy Zhang <zhangyong232@gmail.com>
**
** This file is part of DroidPlugin.
**
** DroidPlugin is free software: you can redistribute it and/or
** modify it under the terms of the GNU Lesser General Public
** License as published by the Free Software Foundation, either
** version 3 of the License, or (at your option) any later version.
**
** DroidPlugin is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
** Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public
** License along with DroidPlugin.  If not, see <http://www.gnu.org/licenses/lgpl.txt>
**
**/

package com.chen.plugin.hook;

import android.app.Application;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import com.chen.easyplugin.utils.LogUtils;
import com.chen.plugin.hook.proxy.IActivityManagerHook;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Andy Zhang(zhangyong232@gmail.com) on 2015/3/2.
 */
public class HookFactory {

    private static final String TAG = HookFactory.class.getSimpleName();
    private static HookFactory sInstance = null;

    private HookFactory() {
    }

    public static HookFactory getInstance() {
        synchronized (HookFactory.class) {
            if (sInstance == null) {
                sInstance = new HookFactory();
            }
        }
        return sInstance;
    }


    private List<BaseHook> mHookList = new ArrayList<BaseHook>(3);

    public void setHookEnable(boolean enable) {
        synchronized (mHookList) {
            for (BaseHook hook : mHookList) {
                hook.setEnable(enable);
            }
        }
    }

    public void setHookEnable(boolean enable, boolean reinstallHook) {
        synchronized (mHookList) {
            for (BaseHook hook : mHookList) {
                hook.setEnable(enable, reinstallHook);
            }
        }
    }

    public void setHookEnable(Class hookClass, boolean enable) {
        synchronized (mHookList) {
            for (BaseHook hook : mHookList) {
                if (hookClass.isInstance(hook)) {
                    hook.setEnable(enable);
                }
            }
        }
    }

    public void installHook(BaseHook hook, ClassLoader cl) {
        try {
            hook.onInstall(cl);
            synchronized (mHookList) {
                mHookList.add(hook);
            }
        } catch (Throwable throwable) {
            LogUtils.e(TAG, "installHook %s error", throwable, hook);
        }
    }


    public final void installHook(Context context, ClassLoader classLoader) throws Throwable {


        //installHook(new IPackageManagerHook(context), classLoader);
        installHook(new IActivityManagerHook(context), classLoader);

//        installHook(new IClipboardBinderHook(context), classLoader);
//        //for ISearchManager
//        installHook(new ISearchManagerBinderHook(context), classLoader);
//        //for INotificationManager
//        installHook(new INotificationManagerBinderHook(context), classLoader);
//        installHook(new IMountServiceBinder(context), classLoader);
//        installHook(new IAudioServiceBinderHook(context), classLoader);
//        installHook(new IContentServiceBinderHook(context), classLoader);
//        installHook(new IWindowManagerBinderHook(context), classLoader);
//        if (VERSION.SDK_INT > VERSION_CODES.LOLLIPOP_MR1) {
//            installHook(new IGraphicsStatsBinderHook(context), classLoader);
//        }
////        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
////            installHook(new WebViewFactoryProviderHook(context), classLoader);
////        }
//        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
//            installHook(new IMediaRouterServiceBinderHook(context), classLoader);
//        }
//        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
//            installHook(new ISessionManagerBinderHook(context), classLoader);
//        }
//        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
//            installHook(new IWifiManagerBinderHook(context), classLoader);
//        }
//
//        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
//            installHook(new IInputMethodManagerBinderHook(context), classLoader);
//        }
//        if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
//            installHook(new ILocationManagerBinderHook(context), classLoader);
//        }
//
//        installHook(new PluginCallbackHook(context), classLoader);
//        installHook(new InstrumentationHook(context), classLoader);
//        installHook(new LibCoreHook(context), classLoader);
//
//        installHook(new SQLiteDatabaseHook(context), classLoader);
    }

    public final void onCallApplicationOnCreate(Context context, Application app) {
//        installHook(new SQLiteDatabaseHook(context), app.getClassLoader());
    }
}

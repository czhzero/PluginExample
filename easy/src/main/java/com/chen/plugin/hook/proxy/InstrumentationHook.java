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

package com.chen.plugin.hook.proxy;

import android.app.Instrumentation;
import android.content.Context;


import com.chen.easyplugin.utils.LogUtils;
import com.chen.plugin.hook.BaseHook;
import com.chen.plugin.hook.BaseHookHandle;
import com.chen.plugin.hook.handle.PluginInstrumentation;
import com.chen.plugin.reflect.FieldUtils;
import com.chen.plugin.utils.compat.ActivityThreadCompat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andy Zhang(zhangyong232@gmail.com) on 2015/3/2.
 */
public class InstrumentationHook extends BaseHook {

    private static final String TAG = InstrumentationHook.class.getSimpleName();
    private List<PluginInstrumentation> mPluginInstrumentations = new ArrayList<>();

    public InstrumentationHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return null;
    }

    @Override
    public void setEnable(boolean enable, boolean reinstallHook) {
        if (reinstallHook) {
            try {
                onInstall(null);
            } catch (Throwable throwable) {
                LogUtils.i(TAG, "setEnable onInstall fail", throwable);
            }
        }

        for (PluginInstrumentation pit : mPluginInstrumentations) {
            pit.setEnable(enable);
        }

        super.setEnable(enable,reinstallHook);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {

        Object target = ActivityThreadCompat.currentActivityThread();
        Class ActivityThreadClass = ActivityThreadCompat.activityThreadClass();

         /*替换ActivityThread.mInstrumentation，拦截组件调度消息*/
        Field mInstrumentationField = FieldUtils.getField(ActivityThreadClass, "mInstrumentation");
        Instrumentation mInstrumentation = (Instrumentation) FieldUtils.readField(mInstrumentationField, target);
        if (!PluginInstrumentation.class.isInstance(mInstrumentation)) {
            PluginInstrumentation pit = new PluginInstrumentation(mHostContext, mInstrumentation);
            pit.setEnable(isEnable());
            mPluginInstrumentations.add(pit);
            FieldUtils.writeField(mInstrumentationField, target, pit);
            LogUtils.i(TAG, "Install Instrumentation Hook old=%s,new=%s", mInstrumentationField, pit);
        } else {
            LogUtils.i(TAG, "Instrumentation has installed,skip");
        }
    }
}

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

package com.chen.plugin.hook.binder;

import android.content.Context;
import android.os.IBinder;

import com.chen.plugin.helper.compat.IMountServiceCompat;
import com.chen.plugin.hook.BaseHookHandle;
import com.chen.plugin.hook.handle.IMountServiceHookHandle;


/**
 * Created by Andy Zhang(zhangyong232@gmail.com) on 2015/3/6.
 */
public class IMountServiceBinderHook extends BinderHook {

    private final static String SERVICE_NAME = "mount";

    public IMountServiceBinderHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    public Object getOldObj() throws Exception{
        IBinder iBinder = MyServiceManager.getOriginService(SERVICE_NAME);
        return IMountServiceCompat.asInterface(iBinder);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IMountServiceHookHandle(mHostContext);
    }

}

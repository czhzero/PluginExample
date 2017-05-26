package com.chen.plugin.hook;

import android.content.Context;

import com.chen.plugin.hook.HookedMethodHandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenzhaohua on 17/5/16.
 */

public abstract class BaseHookHandle {

    protected Context mHostContext;

    protected Map<String, HookedMethodHandler> sHookedMethodHandlers = new HashMap<String, HookedMethodHandler>(5);

    public BaseHookHandle(Context hostContext) {
        mHostContext = hostContext;
        init();
    }

    protected abstract void init();

    public Set<String> getHookedMethodNames() {
        return sHookedMethodHandlers.keySet();
    }

    public HookedMethodHandler getHookedMethodHandler(Method method) {
        if (method != null) {
            return sHookedMethodHandlers.get(method.getName());
        } else {
            return null;
        }
    }

}

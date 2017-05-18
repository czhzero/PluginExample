package com.chen.plugin.pm;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by chenzhaohua on 17/5/18.
 */

public class PluginManager {

    private class PluginManagerServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            android.util.Log.d("czh", "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            android.util.Log.d("czh", "onServiceConnected");
        }

    }

}

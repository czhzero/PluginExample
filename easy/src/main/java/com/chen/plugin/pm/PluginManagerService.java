package com.chen.plugin.pm;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by chenzhaohua on 17/5/18.
 *
 * 插件管理服务
 *
 */
public class PluginManagerService extends Service {

    private static final String TAG = PluginManagerService.class.getSimpleName();
    private PluginManagerServiceImpl mPluginManagerServiceBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        keepAlive();
        mPluginManagerServiceBinder = new PluginManagerServiceImpl(this);
        mPluginManagerServiceBinder.onCreate();

    }

    private void keepAlive() {
        try {
            Notification notification = new Notification();
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            startForeground(0, notification); // 设置为前台服务避免kill，Android4.3及以上需要设置id为0时通知栏才不显示该通知；
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        try {
            mPluginManagerServiceBinder.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mPluginManagerServiceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        //这里要处理IntentService
//        IActivityManagerHookHandle.getIntentSender.handlePendingIntent(this, intent);
        return super.onStartCommand(intent, flags, startId);
    }


}

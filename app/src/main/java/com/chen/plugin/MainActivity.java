package com.chen.plugin;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;

import com.chen.plugin.pm.PluginManager;


/**
 * Created by chenzhaohua on 17/4/12.
 */

public class MainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        findViewById(R.id.tv_entrance_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.amap.map3d.demo", "com.amap.map3d.demo.MainActivity");
                startActivity(intent);
            }
        });

        findViewById(R.id.tv_entrance_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.chen.testapp", "com.chen.testapp.MainActivity");
                startActivity(intent);
            }
        });


        findViewById(R.id.tv_entrance_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PluginManager.getInstance().installPackage("/sdcard/test2.apk", 1);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });


        findViewById(R.id.tv_entrance_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                PackageManager pm = getPackageManager();
//                Intent intent = pm.getLaunchIntentForPackage("com.chen.testapp");
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
                PluginApplication.applicationOnCreate(MainActivity.this.getApplicationContext());
            }
        });



    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);


//        try {
//            FrameworkHookHelper.hookActivityManagerNative();
//            FrameworkHookHelper.hookActivityThreadHandler();
//            FrameworkHookHelper.hookPackageManager();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }




}

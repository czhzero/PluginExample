package test.chen.com.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;

import com.chen.plugin.PluginHelper;
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
                PluginHelper.getInstance().initPlugin(MainActivity.this);
            }
        });

        findViewById(R.id.tv_entrance_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PluginManager.getInstance().installPackage("/sdcard/test1.apk", 0);
                    PluginManager.getInstance().installPackage("/sdcard/test2.apk", 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });


    }



}

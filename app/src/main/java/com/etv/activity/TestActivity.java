package com.etv.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.etv.service.TcpSocketService;
import com.etv.util.SharedPerManager;
import com.ys.etv.R;


public class TestActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        startService(new Intent(TestActivity.this, TcpSocketService.class));
        initView();
        SharedPerManager.setSocketIpAddress("192.168.7.162");
        SharedPerManager.setUserName("admin", "准备连接服务器，这里保存一次");
        SharedPerManager.setSocketPort(9222);

//        SharedPerManager.setWebHost("121.37.240.228");
//        SharedPerManager.setUserName("admin", "准备连接服务器，这里保存一次");
//        SharedPerManager.setWebPort("9222");

    }

    private void initView() {
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TcpSocketService.getInstance().dissOrReconnect();
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDevOnLine();
            }
        });
    }

    private void checkDevOnLine() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TcpSocketService.getInstance().dissOrReconnect();
                        }
                    });
                }
            }
        }.start();
    }

}
package com.atc0194.huge_homework;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import vendor.autochips.hardware.homework.V1_0.IDemo;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    protected Handler handler = new Handler();
    protected IDemo service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final DashBoard d = (DashBoard) findViewById(R.id.dash);

        findViewById(R.id.rand).setOnClickListener(view -> {
            int max = 120;
            int min = 1;
            Random random = new Random();
            int p = random.nextInt(max) % (max - min + 1) + min;
            d.cgangePer(p / 120f);
            FlashHelper.getInstance().startFlick(findViewById(R.id.rand));
        });

        findViewById(R.id.retu).setOnClickListener(view -> d.cgangePer(0));

        try {
            service = IDemo.getService();
            service.opendev();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        new Thread(){
            @Override
            public void run() {
                handler.post(() -> {
                    try {
                        d.cgangePer(service.writedev() / 120f);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
            }
        }.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //service.closedev();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}

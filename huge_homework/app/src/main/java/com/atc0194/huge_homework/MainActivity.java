package com.atc0194.huge_homework;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import vendor.autochips.hardware.homework.V1_0.IDemo;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final DashBoard d = (DashBoard) findViewById(R.id.dash);

        findViewById(R.id.rand).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int max = 120;
                int min = 1;
                Random random = new Random();
                int p = random.nextInt(max) % (max - min + 1) + min;
                d.cgangePer(p / 120f);
                IDemo iDemo = null;
                try {
                    iDemo = IDemo.getService();
                    int fd = iDemo.opendev();
                    Toast.makeText(getApplicationContext(), "fd = " + fd, Toast.LENGTH_LONG).show();
                    if(fd == 0 || fd == 1)FlashHelper.getInstance().startFlick(findViewById(R.id.rand));
                    Log.d(TAG, "re: " + fd);
                    System.out.println(fd);
                }catch (RemoteException e) {
                    e.printStackTrace();
                    Log.d(TAG, "onClick: Exception" );
                }


            }
        });

        findViewById(R.id.retu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.cgangePer(0);
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}

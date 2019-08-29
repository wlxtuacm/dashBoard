package com.atc0194.huge_homework;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import vendor.autochips.hardware.dashboard.V1_0.IDashBoard;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    protected DashBoard massDash;
    protected DashBoard mileageDash;
    protected ImageView leftTurnSignal;
    protected Button reset;
    protected Spinner nodeSpinner;

    protected Handler handler = new Handler();
    protected IDashBoard service;

    protected int fd;
    protected String str;

    protected String[] cmds;
    protected boolean turnLeft;
    protected int mass;
    protected int mileage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        massDash = findViewById(R.id.mass_dash);
        mileageDash = findViewById(R.id.mileage_dash);
        leftTurnSignal = findViewById(R.id.leftTurnSignal);
        reset = findViewById(R.id.reset);
        nodeSpinner = findViewById(R.id.nodeSpinner);

        findViewById(R.id.rand).setOnClickListener(view -> {
            int max = 120;
            int min = 1;
            Random random = new Random();
            int p = random.nextInt(max) % (max - min + 1) + min;
            turnLeft = p < 60;
            massDash.cgangePer(p / 120f);
            mileageDash.cgangePer(p / 120f);
            if(turnLeft)
                FlashHelper.getInstance().startFlick(leftTurnSignal);
            else
                FlashHelper.getInstance().stopFlick(leftTurnSignal);
        });

        reset.setOnClickListener(view -> {
            massDash.cgangePer(0);
            mileageDash.cgangePer(0);
            FlashHelper.getInstance().stopFlick(leftTurnSignal);
        });

        //try {
            //service = IDashBoard.getService();
            String nodeName = nodeSpinner.getSelectedItem().toString();
            Log.d(TAG, "nodeName: " + nodeName);
            //fd = service.dashBoard_open(nodeName.toLowerCase());
        //} catch (RemoteException e) {
        //    e.printStackTrace();
        //}

        /*new Thread(){
            @Override
            public void run() {
                for (; ;) {
                    try {
                        str = service.dashBoard_read(fd);
                        cmds = str.split("_");
                        Log.d(TAG, "cmds: " + cmds);

                        turnLeft = cmds[0].equals("1");
                        mass = Integer.valueOf(cmds[1]);
                        mileage = Integer.valueOf(cmds[2]);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    handler.post(() -> {
                        updateUI();
                    });
                }
            }
        }.start();*/

    }

    private void updateUI(){
        if(turnLeft)
            FlashHelper.getInstance().startFlick(leftTurnSignal);
        else
            FlashHelper.getInstance().stopFlick(leftTurnSignal);

        massDash.cgangePer(mass / 120f);
        mileageDash.cgangePer(mileage / 120f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*try {
            service.dashBoard_close(fd);
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}

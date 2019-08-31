package com.atc0194.huge_homework;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Random;

/**
 * Title:MainActivity
 * Description:前台Activity，实时显示和更新UI
 * Created by atc0190
 * Date: 2019/8/23
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    protected DashBoardView massDash;
    protected DashBoardView mileageDash;
    protected ImageView leftTurnSignal;
    protected Button reset;
    protected Spinner nodeSpinner;

    private IDashBoardServiceInterface dashBoardServiceProxy;
    private ServiceConnection dashBoardServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            dashBoardServiceProxy = IDashBoardServiceInterface.Stub.asInterface(iBinder);
            Log.d(TAG, "onServiceConnected: " + dashBoardServiceProxy);
            try {
                dashBoardServiceProxy.registerCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            try {
                dashBoardServiceProxy.unregisterCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            dashBoardServiceProxy = null;
        }
    };

    //data read from driver through hal
    protected String[] data;
    protected boolean turnLeft;
    protected int mass;
    protected int mileage;
    protected int speed;

    //a handler for those operations like updating UI
    protected Handler handler = new Handler();

    //a thread for reading data continuously from DashBoardService by polling
    // , which has been replaced by Callback
    @Deprecated
    protected Thread pollingThread = new Thread(){
        @Override
        public void run() {
            while(dashBoardServiceProxy == null);
            for (;;) {
                try {
                    if(!parseData(dashBoardServiceProxy.getData())){
                        continue;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                updateUI();
            }
        }
    };

    //Callback from DashBoardService
    private IDashBoardCallback mCallback = new IDashBoardCallback.Stub() {
        @Override
        public void onResult(String rawData) throws RemoteException {
            parseData(rawData);
            updateUI();
        }
    };

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
            mass = random.nextInt(max) % (max - min + 1) + min;
            speed = random.nextInt(max) % (max - min + 1) + min;
            mileage = random.nextInt(max) % (max - min + 1) + min;
            turnLeft = random.nextInt(max) % (max - min + 1) + min > 60;

            updateUI();

        });

        reset.setOnClickListener(view -> {
            mass = speed = mileage = 0;
            turnLeft = false;

            updateUI();
        });

        bindService(new Intent(MainActivity.this, DashBoardService.class),
                dashBoardServiceConnection, BIND_AUTO_CREATE);

        nodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    if(dashBoardServiceProxy != null) {
                        dashBoardServiceProxy.setBusType(adapterView.getItemAtPosition(i).toString());
                    }
                    Log.d(TAG, "onItemSelected: " + adapterView.getItemAtPosition(i));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //pollingThread.start();
    }

    private boolean parseData(@NonNull String rawData) {
        String[] strs = rawData.split("_");
        Log.d(TAG, "data: " + Arrays.toString(strs));

        if(Arrays.equals(data, strs)) {
            return false;
        }

        data = strs;
        turnLeft = data[0].equals("1");
        mass = Integer.valueOf(data[1]);
        mileage = Integer.valueOf(data[2]);
        speed = Integer.valueOf(data[3]);

        return true;
    }

    private void updateUI() {
        handler.post(() -> {
            if (turnLeft)
                FlashHelper.getInstance().startFlick(leftTurnSignal);
            else
                FlashHelper.getInstance().stopFlick(leftTurnSignal);

            massDash.cgangePer(mass / 120f);
            mileageDash.cgangePer(mileage / 120f);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //pollingThread.interrupt();
        unbindService(dashBoardServiceConnection);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}

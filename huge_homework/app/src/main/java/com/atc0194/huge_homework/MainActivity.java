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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    private SpeedDashBoardView speedDash;
    private MassDashBoardView massDash;
    private ImageView leftTurnSignal;
    private Button reset;
    private Spinner nodeSpinner;

    private volatile IDashBoardServiceInterface dashBoardServiceProxy;
    private final ServiceConnection dashBoardServiceConnection = new ServiceConnection() {
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
    private DashBoardData data = new DashBoardData();

    //a handler for those operations like updating UI
    private final Handler handler = new Handler();

    //a thread for reading data continuously from DashBoardService by polling
    // , which has been replaced by Callback
    @Deprecated
    protected Thread pollingThread = new Thread(){
        @Override
        public void run() {
            while(dashBoardServiceProxy == null);
            String rawData;
            for (;;) {
                try {
                    rawData = dashBoardServiceProxy.getData();
                    if(!data.checkIsSame(rawData)){
                        continue;
                    }
                    data.parseData(rawData);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e){
                    e.printStackTrace();
                    handler.post(() -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                    continue;
                }
                updateUI();
            }
        }
    };

    //Callback from DashBoardService
    private final IDashBoardCallback mCallback = new IDashBoardCallback.Stub() {
        @Override
        public void onResult(String rawData) throws RemoteException {
            try {
                data.parseData(rawData);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
                handler.post(() -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                return;
            }
            updateUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speedDash = findViewById(R.id.speed_dash);
        massDash = findViewById(R.id.mass_dash);
        leftTurnSignal = findViewById(R.id.leftTurnSignal);
        reset = findViewById(R.id.reset);
        nodeSpinner = findViewById(R.id.nodeSpinner);

        findViewById(R.id.rand).setOnClickListener(view -> {
            int max = 120;
            int min = 1;
            Random random = new Random();
            data.setData(random.nextInt(max) % (max - min + 1) + min > 60,
                    random.nextInt(max) % (max - min + 1) + min,
                    random.nextInt(max) % (max - min + 1) + min,
                    random.nextInt(max) % (max - min + 1) + min);
            Log.d(TAG, "emulation " + data.getData());

            updateUI();

        });

        reset.setOnClickListener(view -> {
            data.reset();
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

    private void updateUI() {
        handler.post(() -> {
            if (data.isTurnLeft()) {
                FlashHelper.getInstance().startFlick(leftTurnSignal);
            } else {
                FlashHelper.getInstance().stopFlick(leftTurnSignal);
            }

            speedDash.cgangePer(data.getSpeed());
            massDash.cgangePer(data.getMass());
            massDash.setMileage(data.getMileage());
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

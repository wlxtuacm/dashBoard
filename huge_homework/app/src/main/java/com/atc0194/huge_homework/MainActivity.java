package com.atc0194.huge_homework;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import vendor.autochips.hardware.dashboard.V1_0.CarInfoData;

/**
 * Title:MainActivity
 * Description:前台Activity，实时显示和更新UI
 * Created by atc0190
 * Date: 2019/8/23
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SpeedDashBoardView speedDash;
    private MassDashBoardView massDash;
    private ImageView leftTurnSignal;

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
            } catch (NullPointerException e){
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
    private final DashBoardData data = new DashBoardData(new CarInfoData());

    //a handler for those operations like updating UI
    private final Handler handler = new Handler();

    //Callback from DashBoardService
    private final IDashBoardCallback mCallback = new IDashBoardCallback.Stub() {
        @Override
        public void onResult(String rawData) throws RemoteException {
            try {
                Log.d(TAG, "rawData: " + rawData);
                data.fromString(rawData);
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

        bindService(new Intent(MainActivity.this, DashBoardService.class),
                dashBoardServiceConnection, BIND_AUTO_CREATE);
    }

    private void updateUI() {
        handler.post(() -> {
            if (data.isTurnleft()) {
                FlashHelper.getInstance().startFlick(leftTurnSignal);
            } else {
                FlashHelper.getInstance().stopFlick(leftTurnSignal);
            }

            //workaround for speed > 127
            speedDash.cgangePer(data.getSpeed() & 0x0ff);
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
}


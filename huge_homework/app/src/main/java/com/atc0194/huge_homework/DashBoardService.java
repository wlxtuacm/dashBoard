package com.atc0194.huge_homework;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.NoSuchElementException;
import java.util.Random;

import vendor.autochips.hardware.dashboard.V1_0.CarInfoData;
import vendor.autochips.hardware.dashboard.V1_0.IDashBoard;

/**
 * Title:DashBoardService
 * Description:后台Service，实时获取驱动信息
 * Created by atc0190
 * Date: 2019/8/23
 */
public class DashBoardService extends Service {

    private static final String TAG = "DashBoardService";

    private IDashBoard service;

    //a wrapper of carInfo read from driver
    private DashBoardData rawData = new DashBoardData(new CarInfoData());

    //hardcode only for test mode
    private boolean testMode;
    private DashBoardData emulationData;

    private Thread pollingThread = new Thread(() -> {
        for(;;) {
            if(!testMode) {
                try {
                    if(rawData.setRawDataAfterCheckIsSame(service.dashBoard_getInfo())){
                        continue;
                    }
                    Log.d(TAG, "rawData: " + rawData.getRawData());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }else {
                if(rawData.setRawDataAfterCheckIsSame(emulationData.getRawData())){
                    continue;
                }
            }

            try {
                Log.d(TAG, "callback rawData: " + rawData);
                callback(rawData.toString());
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    public DashBoardService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        //android.os.Debug.waitForDebugger();
        Log.d(TAG, "onBind: ");
        try {
            service = IDashBoard.getService();
            if(!service.dashBoard_init()) {
                Toast.makeText(getApplicationContext(), "init fail!", Toast.LENGTH_LONG).show();
                Log.e(TAG, "init fail!");
                return null;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            Log.e(TAG, "getService fail, now in test mode!");
            Toast.makeText(getApplicationContext(), "getService fail, now in test mode!", Toast.LENGTH_LONG).show();
            testMode = true;
            emulationData = new DashBoardData(new CarInfoData());

            new Thread(() -> {
                Random random = new Random();
                for(;;) {
                    emulationData.setTurnleft(random.nextInt(2) >= 1);
                    emulationData.setMass((byte) random.nextInt(DashBoardData.MAX_MASS + 20));
                    emulationData.setMileage(random.nextInt(DashBoardData.MAX_MILEAGE + 10000));
                    emulationData.setSpeed((byte) random.nextInt(DashBoardData.MAX_SPEED + 50));
                    Log.d(TAG, "emulationData " + emulationData.getRawData());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }).start();
        }

        pollingThread.start();
        return dashBoardServiceStub;
    }

    private final RemoteCallbackList<IDashBoardCallback> mCallbacks =
            new RemoteCallbackList<>();

    private final IDashBoardServiceInterface.Stub dashBoardServiceStub =
            new IDashBoardServiceInterface.Stub() {

        @Override
        public void registerCallback(IDashBoardCallback cb) throws RemoteException {
            if(cb != null) {
                mCallbacks.register(cb);
            }
        }

        @Override
        public void unregisterCallback(IDashBoardCallback cb) throws RemoteException {
            if(cb != null) {
                mCallbacks.unregister(cb);
            }
        }
    };

    private void callback(String rawData) throws RemoteException {
        final int len = mCallbacks.beginBroadcast();
        for(int i = 0; i < len; i++) {
            mCallbacks.getBroadcastItem(i).onResult(rawData);
        }

        mCallbacks.finishBroadcast();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pollingThread.interrupt();
        mCallbacks.kill();
        if(!testMode) {
            try {
                service.dashBoard_deinit();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}

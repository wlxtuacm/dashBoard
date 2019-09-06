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

    //Info from driver
    private CarInfoData rawInfo;
    //last different rawInfo
    private CarInfoData preInfo;

    //String parse from rawInfo
    private String rawData;

    //hardcode only for test mode
    private boolean testMode;
    private DashBoardData emulationData;

    public DashBoardService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");

        new Thread(() -> {
            for(;;) {
                if(!testMode) {
                    try {
                        rawInfo = service.dashBoard_getInfo();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }else {
                    rawData = emulationData.getData();
                }

                if (preInfo.equals(rawInfo)) {
                    continue;
                }else {
                    rawData = "" + (rawInfo.turnleft ? 1 : 0) + rawInfo.mass + rawInfo.mileage +
                            rawInfo.speed;
                }

                try {
                    Log.d(TAG, "callback rawData: " + rawData);
                    callback(rawData);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                preInfo = rawInfo;
            }
        }).start();

        return dashBoardServiceStub;
    }

    private final RemoteCallbackList<IDashBoardCallback> mCallbacks =
            new RemoteCallbackList<>();

    private final IDashBoardServiceInterface.Stub dashBoardServiceStub =
            new IDashBoardServiceInterface.Stub() {

        @Override
        public String getData() throws RemoteException {
            return rawData;
        }

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
        for(int i = 0; i < len;i++) {
            mCallbacks.getBroadcastItem(i).onResult(rawData);
        }

        mCallbacks.finishBroadcast();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            service = IDashBoard.getService();
            if(!service.dashBoard_init()) {
                Toast.makeText(getApplicationContext(), "init fail!", Toast.LENGTH_LONG).show();
                Log.e(TAG, "init fail!");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            Log.e(TAG, "getService fail, now in test mode!");
            Toast.makeText(getApplicationContext(), "getService fail, now in test mode!", Toast.LENGTH_LONG).show();
            testMode = true;
            emulationData = new DashBoardData();

            new Thread(() -> {
                Random random = new Random();
                for(;;) {
                    emulationData.setData(random.nextInt(2) >= 1,
                            random.nextInt(DashBoardData.MAX_MASS + 20),
                            random.nextInt(DashBoardData.MAX_MILEAGE + 10000),
                            random.nextInt(DashBoardData.MAX_SPEED + 50));
                    Log.d(TAG, "emulationData " + emulationData.getData());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

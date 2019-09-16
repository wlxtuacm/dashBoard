package com.atc0194.huge_homework;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.NoSuchElementException;

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

    private Thread pollingThread = new Thread(() -> {
        for(;;) {
            try {
                if(rawData.setRawDataAfterCheckIsSame(service.dashBoard_getInfo())){
                    continue;
                }
                Log.d(TAG, "rawData: " + rawData.getRawData());
            } catch (RemoteException e) {
                e.printStackTrace();
            }


            try {
                Log.d(TAG, "callback rawData: " + rawData);
                callback(rawData.toString());
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(100);
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
        } catch (NoSuchElementException | RemoteException e) {
            e.printStackTrace();
            Log.e(TAG, "getService fail!");
            Toast.makeText(getApplicationContext(), "getService fail!", Toast.LENGTH_LONG).show();
            return null;
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
        try {
            service.dashBoard_deinit();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}

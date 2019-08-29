package com.atc0194.huge_homework;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.Random;

import vendor.autochips.hardware.dashboard.V1_0.IDashBoard;

public class DashBoardService extends Service {

    private static final String TAG = "DashBoardService";

    protected IDashBoard service;

    protected int fd;

    private String busType = "I2C";
    private String rawData = "";
    private String preData = "";

    public DashBoardService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        new Thread(() -> {
            for(;;) {
                /*try {
                    rawData = service.dashBoard_read(fd);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }*/
                if(busType.equals("I2C"))
                    rawData = "1_90_24_12";
                else if(busType.equals("SPI")){
                    rawData = "0_70_30_13";
                }else{
                    rawData = "1_100_12_89";
                }
                if(preData.equals(rawData))
                    continue;

                try {
                    Log.d(TAG, "callback: ");
                    callback(rawData);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                preData = rawData;


            }
        }).start();

        return dashBoardServiceStub;
    }

    private final RemoteCallbackList<IDashBoardCallback> mCallbacks =
            new RemoteCallbackList<>();

    public IDashBoardServiceInterface.Stub dashBoardServiceStub =
            new IDashBoardServiceInterface.Stub() {
        @Override
        public void setBusType(String busType) throws RemoteException {
            DashBoardService.this.busType = busType;
        }

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
        Log.d(TAG, "onCreate: ");
        super.onCreate();
        /*try {
        service = IDashBoard.getService();
        fd = service.dashBoard_open(busType);
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*try {
            service.dashBoard_close(fd);
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/
    }
}

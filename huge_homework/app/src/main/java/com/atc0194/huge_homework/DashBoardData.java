package com.atc0194.huge_homework;

import android.content.Intent;

import vendor.autochips.hardware.dashboard.V1_0.CarInfoData;

import androidx.annotation.NonNull;

/**
 * Title:DashBoardData
 * Description:封装DashBoard数据和方法的实体类
 * Created by atc0190
 * Date: 2019/9/03
 */
class DashBoardData {

    private static final String TAG = "DashBoardData";

    //max mileage: 100w km
    public static final int MAX_MILEAGE = 100_0000;
    //max speed: 180 km/h
    public static final int MAX_SPEED = 180;
    //max speed: 100
    public static final int MAX_MASS = 100;

    private CarInfoData rawData;

    public DashBoardData(CarInfoData rawData) {
        this.rawData = rawData;
    }

    public boolean isTurnleft() {
        return rawData.turnleft;
    }

    public void setTurnleft(boolean turnleft) {
        this.rawData.turnleft = turnleft;
    }

    public int getMass() {
        return rawData.mass;
    }

    public void setMass(byte mass) {
        this.rawData.mass = mass;
    }

    public int getMileage() {
        return rawData.mileage;
    }

    public void setMileage(int mileage) {
        this.rawData.mileage = mileage;
    }

    public int getSpeed() {
        return rawData.speed;
    }

    public void setSpeed(byte speed) {
        this.rawData.speed = speed;
    }

    private void checkParamsRange() {
        if(getMass() > MAX_MASS) {
            throw new IllegalArgumentException("mass " + getMass() +
                    " is out of range [0," + MAX_MASS + "]");
        }

        if(getMileage() >= MAX_MILEAGE) {
            throw new IllegalArgumentException("mileage " + getMileage() +
                    " is out of range [0," + MAX_MILEAGE + ")");
        }

        if(getSpeed() > MAX_SPEED) {
            throw new IllegalArgumentException("speed " + getSpeed() +
                    " is out of range [0," + MAX_SPEED + "]");
        }
    }

    public boolean checkIsSame(@NonNull CarInfoData newData) {
        return rawData.equals(newData);
    }


    public CarInfoData getRawData() {
        return rawData;
    }

    public void setRawData(CarInfoData rawData) {
        this.rawData = rawData;
    }

    public boolean setRawDataAfterCheckIsSame(@NonNull CarInfoData rawData) {
        if(checkIsSame(rawData))
            return true;
        this.rawData = rawData;

        return false;
    }

    @Override
    public String toString() {
        return "" + (isTurnleft() ? 1 : 0) + "_" + getMass() + "_" + getMileage() + "_"
                + getSpeed();
    }

    public void fromString(@NonNull String str) {
        String[] data = str.split("_");

        if(rawData == null) {
            setRawData(new CarInfoData());
        }

        setTurnleft(data[0].equals("1"));
        setMass(Byte.valueOf(data[1]));
        setMileage(Integer.valueOf(data[2]));
        setSpeed(Byte.valueOf(data[3]));

        checkParamsRange();
    }

}

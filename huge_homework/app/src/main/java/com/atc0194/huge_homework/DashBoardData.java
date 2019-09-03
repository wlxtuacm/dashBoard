package com.atc0194.huge_homework;

import android.util.Log;

import androidx.annotation.NonNull;

/**
 * Title:DashBoardData
 * Description:封装DashBoard数据和方法的实体类
 * Created by atc0190
 * Date: 2019/9/03
 */
public class DashBoardData {

    private static final String TAG = "DashBoardData";

    //max mileage: 100w km
    private static final int MAX_MILEAGE = 100_0000;
    //max speed: 180 km/h
    private static final int MAX_SPEED = 180;

    private static final int paramsNum = 4;

    private static final String SPLITCHAR = "_";

    private String lastRawData = "";
    private String[] spitData;

    private boolean turnLeft;
    private int mass;
    private int mileage;
    private int speed;

    public boolean isTurnLeft() {
        return turnLeft;
    }

    public void setTurnLeft(boolean turnLeft) {
        this.turnLeft = turnLeft;
    }

    public int getMass() {
        return mass;
    }

    public void setMass(int mass) {
        this.mass = mass;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    private void checkParamsRange() {
        if(Integer.valueOf(spitData[2]) < 0 || Integer.valueOf(spitData[2]) >= MAX_MILEAGE) {
            throw new IllegalArgumentException("mileage " + spitData[2] +
                    " is out of range [0," + MAX_MILEAGE + ")");
        }

        if(Integer.valueOf(spitData[3]) < 0 || Integer.valueOf(spitData[3]) > MAX_SPEED) {
            throw new IllegalArgumentException("speed " + speed +
                    " is out of range [0," + MAX_SPEED + "]");
        }
    }

    private void checkParamsNum() {
        if(spitData.length < paramsNum) {
            throw new IllegalArgumentException("num of params < " + paramsNum);
        }
    }

    public boolean checkIsSame(@NonNull String rawData) {
        return lastRawData.equals(rawData);
    }

    public void parseData() {
        spitData = lastRawData.split(SPLITCHAR);
        Log.d(TAG, "rawData: " + lastRawData);

        checkParamsNum();

        checkParamsRange();

        turnLeft = spitData[0].equals("1");
        mass = Integer.valueOf(spitData[1]);
        mileage = Integer.valueOf(spitData[2]);
        speed = Integer.valueOf(spitData[3]);
    }

    public void parseData(@NonNull String rawData) {

        if(checkIsSame(rawData)){
            return;
        }
        lastRawData = rawData;

        parseData();
    }

    public void setData(boolean turnLeft, int mass, int mileage, int speed){
        lastRawData = "" + (turnLeft ? 1 : 0) + "_" + mass + "_" +mileage + "_" +speed;
    }

    public String getData() {
        return lastRawData;
    }

    public void reset() {
        lastRawData = "";
        spitData = null;
        turnLeft = false;
        mass = 0;
        mileage = 0;
        speed = 0;
    }

}

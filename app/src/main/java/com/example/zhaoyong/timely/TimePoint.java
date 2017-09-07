package com.example.zhaoyong.timely;

import android.graphics.Point;

/**
 * Created by zhaoyong on 17/9/2.
 */

public class TimePoint {

    private float mX;

    private float mY;

    private String mHour;

    private float mScale;

    public TimePoint(int x, int y, String hour) {
        this.mX = x;
        this.mY = y;
        this.mHour = hour;
        this.mScale = 1f;
    }

    public String getHour() {
        return mHour;
    }

    public void setHour(String mHour) {
        this.mHour = mHour;
    }

    public float getScale() {
        return mScale;
    }

    public void setScale(float mScale) {
        this.mScale = mScale;
    }

    public float getX() {
        return mX;
    }

    public void setX(float mX) {
        this.mX = mX;
    }

    public float getY() {
        return mY;
    }

    public void setY(float mY) {
        this.mY = mY;
    }
}

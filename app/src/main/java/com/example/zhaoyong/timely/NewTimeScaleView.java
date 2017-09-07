package com.example.zhaoyong.timely;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.sql.Time;

/**
 * Created by zhaoyong on 17/9/2.
 */

public class NewTimeScaleView extends View {
    private static final String TAG = "TimeScaleView";

    /**显示时间字体大小*/
    private float mTimeTextSize;
    /**显示时间刻度颜色*/
    private int mTimeTextColor;
    /**刻度尺颜色*/
    private int mScaleColor;
    /**刻度尺宽度*/
    private float mScaleWidth;
    /**刻度尺一个刻度的高度*/
    private float mScaleHeight;
    /**上边距*/
    private float mTopPadding;
    /**下边距*/
    private float mBottomPadding;
    /**刻度尺与时间数值的间距*/
    private float mPaddingTimeWithScale;
    /**刻度尺画笔*/
    private Paint mLinePaint;
    /**时间数值画笔*/
    private Paint mTimeTextPaint;
    /**触摸位置时间小时*/
    private int mTimeHour;
    /**刻度尺的间隔长度*/
    private float mNormalGap;
    /**是否点击屏幕*/
    private boolean mIsTouched;
    /**放大增加系数*/
    private final float[] mScale = {0.25f, 0.5f, 0.75f};
    /**刻度尺时间字符*/
    private final String[] timeText = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
            "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24"};

    private final int TIME_NUM = 25;

    private TimePoint[] mTimeNormalPoints = new TimePoint[TIME_NUM];

    private TimePoint[] mTimeScalePoints = new TimePoint[TIME_NUM];

    public NewTimeScaleView(Context context) {
        this(context, null);
        Log.i(TAG, "TimeScaleView1");
    }

    public NewTimeScaleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        Log.i(TAG, "TimeScaleView2");
    }

    public NewTimeScaleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        Log.i(TAG, "TimeScaleView3");
    }

    public NewTimeScaleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        Log.i(TAG, "TimeScaleView4");

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.timescaleview, defStyleRes, 0);

        mBottomPadding = typedArray.getDimension(R.styleable.timescaleview_bottomPadding,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));

        mScaleColor = typedArray.getColor(R.styleable.timescaleview_scaleColor, Color.BLUE);

        mScaleWidth = typedArray.getDimension(R.styleable.timescaleview_scaleWidth,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));

        mScaleHeight = typedArray.getDimension(R.styleable.timescaleview_scaleHeight,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));

        mTimeTextColor = typedArray.getColor(R.styleable.timescaleview_textColor, Color.BLUE);

        mTimeTextSize = typedArray.getDimension(R.styleable.timescaleview_textSize,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));

        mTopPadding = typedArray.getDimension(R.styleable.timescaleview_topPadding,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));

        mPaddingTimeWithScale = typedArray.getDimension(R.styleable.timescaleview_paddingTimeWithScale,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics()));

        typedArray.recycle();

        //初始化画笔
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setColor(mScaleColor);
        mLinePaint.setStrokeWidth(mScaleHeight);

        mTimeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTimeTextPaint.setTextSize(mTimeTextSize);
        mTimeTextPaint.setColor(mTimeTextColor);

        initTimePoint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //计算未放大时的刻度尺间隔
        mNormalGap = (getHeight() - mTopPadding - mBottomPadding) / 24;
        caculateTimePointLocation();
    }

    private void initTimePoint() {
        for(int i=0; i<TIME_NUM; i++) {
            mTimeNormalPoints[i] = new TimePoint(0, 0, "0");
            mTimeScalePoints[i] = new TimePoint(0, 0, "0");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mIsTouched) {
            drawLargeScale(canvas);
        } else {
            drawNormalScale(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if(event.getY() >= 0 && event.getY() <= getHeight()) {
                    mIsTouched = true;
                    mTimeHour = caculateTime(event.getY());
                    Log.i("yongzhao", "mTimeHour:" + mTimeHour);
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsTouched = false;
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 根据y轴值计算时间
     * @param y
     * @return 时间
     */
    private int caculateTime(float y) {
        return (int) ((y - mTopPadding / 2) / (getHeight() - (mTopPadding + mBottomPadding) / 2) * 24);
    }

    /**
     * 计算放大时未放大部分的刻度尺间隔
     * @return 刻度尺间隔
     */
    /*private float caculateScaleGap() {
        float scaleGap;
        if(mTimeHour == 0 || mTimeHour == 24) {
            scaleGap = mNormalGap * mScale[0] + mNormalGap * mScale[1] + mNormalGap * mScale[2];
        } else if(mTimeHour == 1 || mTimeHour == 23) {
            scaleGap = mNormalGap * mScale[0] + mNormalGap * mScale[1] + mNormalGap * mScale[2] * 2;
        } else if(mTimeHour == 2 || mTimeHour == 22) {
            scaleGap = mNormalGap * mScale[0] + mNormalGap * mScale[1] * 2 + mNormalGap * mScale[2] * 2;
        } else {
            scaleGap = mNormalGap * mScale[0] * 2 + mNormalGap * mScale[1] * 2 + mNormalGap * mScale[2] * 2;
        }
        return (getHeight() - mTopPadding - mBottomPadding - scaleGap) / 24;
    }*/

    /**
     * 绘制未放大时视图
     * @param canvas
     */
    private void drawNormalScale(Canvas canvas) {
        mTimeTextPaint.setTextSize(mTimeTextSize);
        for(int i=0; i<TIME_NUM; i++) {
            canvas.drawLine(mTimeNormalPoints[i].getX(), mTimeNormalPoints[i].getY(),
                    mTimeNormalPoints[i].getX() + mScaleWidth, mTimeNormalPoints[i].getY(), mLinePaint);
            if(i % 6 == 0) {
                canvas.drawText(mTimeNormalPoints[i].getHour(), mTimeNormalPoints[i].getX() + mScaleWidth + mPaddingTimeWithScale,
                        mTimeNormalPoints[i].getY() + mTimeTextSize / 2, mTimeTextPaint);
            }
        }
    }

    private void caculateTimePointLocation() {
        for(int i=0; i<TIME_NUM; i++) {
            mTimeNormalPoints[i].setX(getX());
            mTimeNormalPoints[i].setY(getY() + mTopPadding + i * mNormalGap);
            mTimeNormalPoints[i].setHour(String.valueOf(i));

            mTimeScalePoints[i].setX(getX());
            mTimeScalePoints[i].setY(getY() + mTopPadding + i * mNormalGap);
            mTimeScalePoints[i].setHour(String.valueOf(i));
        }
    }

    private void updateTimeScalePointLocation() {
        float topHalfGap = getTopHalfGap();
        float bottomHalfGap = getBottomHalfGap();
        float gap;

        if(topHalfGap == 0 || bottomHalfGap == 0) {
            gap = topHalfGap + bottomHalfGap;
        } else {
            gap = (topHalfGap + bottomHalfGap + 0.8f * Math.abs(mTimeHour - 12)) / 2;
        }

        Log.i("zhaoyong2", "topHalfGap:" + topHalfGap + " bottomHalfGap:" + bottomHalfGap);

        if(mTimeHour <= 12 && mTimeHour >= 0) {
            mTimeScalePoints[0].setY(mTimeNormalPoints[0].getY());
            mTimeScalePoints[0].setScale(1f);
            if (mTimeHour == 0) {
                mTimeScalePoints[0].setScale(1 + 0.75f);
            }
            for (int i = 1; i < TIME_NUM; i++) {
                if (i == mTimeHour - 2) {
                    mTimeScalePoints[i].setY(mTimeScalePoints[i - 1].getY() + gap * (1 + 0.25f));
                    mTimeScalePoints[i].setScale(1 + 0.25f);
                } else if (i == mTimeHour - 1) {
                    mTimeScalePoints[i].setY(mTimeScalePoints[i - 1].getY() + gap * (1 + 0.5f));
                    mTimeScalePoints[i].setScale(1 + 0.5f);
                } else if (i == mTimeHour) {
                    //mTimeScalePoints[i].setY(mTimeNormalPoints[i].getY());
                    mTimeScalePoints[i].setY(mTimeScalePoints[i - 1].getY() + gap * (1 + 0.75f));
                    mTimeScalePoints[i].setScale(1 + 0.75f);
                } else if (i == mTimeHour + 1) {
                    mTimeScalePoints[i].setY(mTimeScalePoints[i - 1].getY() + gap * (1 + 0.75f));
                    mTimeScalePoints[i].setScale(1 + 0.5f);
                } else if (i == mTimeHour + 2) {
                    mTimeScalePoints[i].setY(mTimeScalePoints[i - 1].getY() + gap * (1 + 0.5f));
                    mTimeScalePoints[i].setScale(1 + 0.25f);
                } else if (i == mTimeHour + 3) {
                    mTimeScalePoints[i].setY(mTimeScalePoints[i - 1].getY() + gap * (1 + 0.25f));
                    mTimeScalePoints[i].setScale(1f);
                } else {
                    mTimeScalePoints[i].setY(mTimeScalePoints[i - 1].getY() + gap);
                    mTimeScalePoints[i].setScale(1f);
                }

                Log.i("location", "ylocation:" + mTimeScalePoints[i].getY());
            }
        } else {
            mTimeScalePoints[TIME_NUM - 1].setY(mTimeNormalPoints[TIME_NUM - 1].getY());
            mTimeScalePoints[TIME_NUM - 1].setScale(1f);
            if (mTimeHour == TIME_NUM - 1) {
                mTimeScalePoints[TIME_NUM - 1].setScale(1 + 0.75f);
            }

            for(int i= TIME_NUM-2; i>=0; i--) {
                if (i == mTimeHour - 3) {
                    mTimeScalePoints[i].setY(mTimeScalePoints[i + 1].getY() - gap * (1 + 0.25f));
                    mTimeScalePoints[i].setScale(1f);
                } else if (i == mTimeHour - 2) {
                    mTimeScalePoints[i].setY(mTimeScalePoints[i + 1].getY() - gap * (1 + 0.5f));
                    mTimeScalePoints[i].setScale(1 + 0.25f);
                } else if (i == mTimeHour - 1) {
                    mTimeScalePoints[i].setY(mTimeScalePoints[i + 1].getY() - gap * (1 + 0.75f));
                    mTimeScalePoints[i].setScale(1 + 0.5f);
                } else if (i == mTimeHour) {
                    //mTimeScalePoints[i].setY(mTimeNormalPoints[i].getY());
                    mTimeScalePoints[i].setY(mTimeScalePoints[i + 1].getY() - gap * (1 + 0.75f));
                    mTimeScalePoints[i].setScale(1 + 0.75f);
                } else if (i == mTimeHour + 1) {
                    mTimeScalePoints[i].setY(mTimeScalePoints[i + 1].getY() - gap * (1 + 0.5f));
                    mTimeScalePoints[i].setScale(1 + 0.5f);
                } else if (i == mTimeHour + 2) {
                    mTimeScalePoints[i].setY(mTimeScalePoints[i + 1].getY() - gap * (1 + 0.25f));
                    mTimeScalePoints[i].setScale(1 + 0.25f);
                } else {
                    mTimeScalePoints[i].setY(mTimeScalePoints[i + 1].getY() - gap);
                    mTimeScalePoints[i].setScale(1f);
                }
            }
        }
    }

    private float getTopHalfGap() {
        float piceses = 0;
        if(mTimeHour == 0) {
            return 0f;
        } else if(mTimeHour == 1) {
            piceses = mTimeHour + 0.75f;
        } else if(mTimeHour == 2) {
            piceses = mTimeHour + 0.5f + 0.75f;
        } else {
            piceses = mTimeHour + 0.25f + 0.5f + 0.75f;
        }

        return (mTimeNormalPoints[mTimeHour].getY() - mTopPadding) / piceses;
    }

    private float getBottomHalfGap() {
        float piceses = 0f;
        if(mTimeHour == TIME_NUM - 1) {
            return 0f;
        } else if(mTimeHour == TIME_NUM - 2) {
            piceses = TIME_NUM - 1 - mTimeHour + 0.75f;
        } else if(mTimeHour == TIME_NUM - 3) {
            piceses = TIME_NUM - 1 - mTimeHour + 0.5f + 0.75f;
        } else {
            piceses = TIME_NUM - 1 - mTimeHour + 0.25f + 0.5f + 0.75f;
        }

        return (getHeight() - mBottomPadding - mTimeNormalPoints[mTimeHour].getY()) / piceses;
    }

    /**
     * 绘制放大时的视图
     * @param canvas
     */
    private void drawLargeScale(Canvas canvas) {

        updateTimeScalePointLocation();

        float tempScaleWidth;
        float tempTimeTextSize;
        for(int i=0; i<TIME_NUM; i++) {
            tempScaleWidth = mTimeScalePoints[i].getScale() * mScaleWidth;
            tempTimeTextSize = mTimeScalePoints[i].getScale() * mTimeTextSize;
            mTimeTextPaint.setTextSize(tempTimeTextSize);
            canvas.drawLine(mTimeScalePoints[i].getX(), mTimeScalePoints[i].getY(),
                    mTimeScalePoints[i].getX() + tempScaleWidth, mTimeScalePoints[i].getY(), mLinePaint);

            if(i%6==0 || mTimeScalePoints[i].getScale()>1f) {
                canvas.drawText(mTimeScalePoints[i].getHour(), mTimeScalePoints[i].getX() + mPaddingTimeWithScale + tempScaleWidth,
                        mTimeScalePoints[i].getY() + tempTimeTextSize / 2, mTimeTextPaint);
            }
        }


        /**
         * 实现有些复杂，冗余，耦合性高，后面有待完善算法，通过创建每个刻度的坐标数组数据结构解耦
         * 将计算和绘制分开
         */
        /*for(int i=0; i<timeText.length; i++) {
            if(mTimeHour >= 3) {
                if (i <= mTimeHour - 3) {
                    tempScaleWidth = mScaleWidth;
                    tempTimeTextSize = mTimeTextSize;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap, getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap, mLinePaint);
                    if (i % 6 == 0) {
                        canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + tempTimeTextSize / 2, mTimeTextPaint);
                    }
                } else if (i == mTimeHour - 2) {
                    tempScaleWidth = mScaleWidth * 1.5f;
                    tempTimeTextSize = mTimeTextSize * 1.5f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + mNormalGap * mScale[0], getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + mNormalGap * mScale[0], mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + mNormalGap * mScale[0] + tempTimeTextSize / 2, mTimeTextPaint);
                } else if (i == mTimeHour - 1) {
                    tempScaleWidth = mScaleWidth * 2f;
                    tempTimeTextSize = mTimeTextSize * 2f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[0])), getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[0])), mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[0])) + tempTimeTextSize / 2, mTimeTextPaint);
                } else if (i == mTimeHour) {
                    tempScaleWidth = mScaleWidth * 2.5f;
                    tempTimeTextSize = mTimeTextSize * 2.5f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[0] + mScale[2])), getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[0] + mScale[2])), mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[0] + mScale[2])) + tempTimeTextSize / 2, mTimeTextPaint);
                } else if (i == mTimeHour + 1) {
                    tempScaleWidth = mScaleWidth * 2f;
                    tempTimeTextSize = mTimeTextSize * 2f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[0] + mScale[2] * 2)), getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[0] + mScale[2] * 2)), mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[0] + mScale[2] * 2)) + tempTimeTextSize / 2, mTimeTextPaint);
                } else if (i == mTimeHour + 2) {
                    tempScaleWidth = mScaleWidth * 1.5f;
                    tempTimeTextSize = mTimeTextSize * 1.5f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] * 2 + mScale[0] + mScale[2] * 2)), getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] * 2 + mScale[0] + mScale[2] * 2)), mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] * 2 + mScale[0] + mScale[2] * 2)) + tempTimeTextSize / 2, mTimeTextPaint);
                } else {
                    tempScaleWidth = mScaleWidth;
                    tempTimeTextSize = mTimeTextSize;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    if (i == 0) {
                        canvas.drawLine(getX(), getY() + mTopPadding, getX() + tempScaleWidth, getY() + mTopPadding, mLinePaint);
                        canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + tempTimeTextSize / 2, mTimeTextPaint);
                    } else {
                        canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] * 2 + mScale[0] * 2 + mScale[2] * 2)), getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] * 2 + mScale[0] * 2 + mScale[2] * 2)), mLinePaint);
                        if (i % 6 == 0) {
                            canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] * 2 + mScale[0] * 2 + mScale[2] * 2)) + tempTimeTextSize / 2, mTimeTextPaint);
                        }
                    }
                }
            } else if(mTimeHour == 0){
                if (i == mTimeHour) {
                    tempScaleWidth = mScaleWidth * 2.5f;
                    tempTimeTextSize = mTimeTextSize * 2.5f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding, getX() + tempScaleWidth, getY() + mTopPadding, mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + tempTimeTextSize / 2, mTimeTextPaint);
                } else if (i == mTimeHour + 1) {
                    tempScaleWidth = mScaleWidth * 2f;
                    tempTimeTextSize = mTimeTextSize * 2f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + mNormalGap * mScale[2], getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + mNormalGap * mScale[2], mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + mNormalGap * mScale[2] + tempTimeTextSize / 2, mTimeTextPaint);
                } else if (i == mTimeHour + 2) {
                    tempScaleWidth = mScaleWidth * 1.5f;
                    tempTimeTextSize = mTimeTextSize * 1.5f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + mNormalGap * (mScale[1] + mScale[2]), getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + mNormalGap * (mScale[1] + mScale[2]), mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + mNormalGap * (mScale[1] + mScale[2]) + tempTimeTextSize / 2, mTimeTextPaint);
                } else {
                    tempScaleWidth = mScaleWidth;
                    tempTimeTextSize = mTimeTextSize;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + mNormalGap * (mScale[1] + mScale[2] + mScale[0]), getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + mNormalGap * (mScale[1] + mScale[2] + mScale[0]), mLinePaint);
                    if (i % 6 == 0) {
                        canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + mNormalGap * (mScale[1] + mScale[2] + mScale[0]) + tempTimeTextSize / 2, mTimeTextPaint);
                    }
                }
            } else if(mTimeHour == 1) {
                if (i == mTimeHour - 1) {
                    tempScaleWidth = mScaleWidth * 2f;
                    tempTimeTextSize = mTimeTextSize * 2f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap, getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap , mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + tempTimeTextSize / 2, mTimeTextPaint);
                } else if (i == mTimeHour) {
                    tempScaleWidth = mScaleWidth * 2.5f;
                    tempTimeTextSize = mTimeTextSize * 2.5f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + mNormalGap *  mScale[2], getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + mNormalGap *  mScale[2], mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + mNormalGap *  mScale[2] + tempTimeTextSize / 2, mTimeTextPaint);
                } else if (i == mTimeHour + 1) {
                    tempScaleWidth = mScaleWidth * 2f;
                    tempTimeTextSize = mTimeTextSize * 2f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[2] * 2)), getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + (mNormalGap * (2 * mScale[2])), mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + (mNormalGap * (2 * mScale[2])) + tempTimeTextSize / 2, mTimeTextPaint);
                } else if (i == mTimeHour + 2) {
                    tempScaleWidth = mScaleWidth * 1.5f;
                    tempTimeTextSize = mTimeTextSize * 1.5f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + 2 * mScale[2])), getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + 2 * mScale[2])), mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + 2 * mScale[2])) + tempTimeTextSize / 2, mTimeTextPaint);
                } else {
                    tempScaleWidth = mScaleWidth;
                    tempTimeTextSize = mTimeTextSize;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[0] + mScale[2] * 2)), getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[0] + mScale[2] * 2)), mLinePaint);
                    if (i % 6 == 0) {
                        canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[0] + mScale[2] * 2)) + tempTimeTextSize / 2, mTimeTextPaint);
                    }
                }
            } else if(mTimeHour == 2) {
                if (i == mTimeHour - 2) {
                    tempScaleWidth = mScaleWidth * 1.5f;
                    tempTimeTextSize = mTimeTextSize * 1.5f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap, getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap, mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + tempTimeTextSize / 2, mTimeTextPaint);
                } else if (i == mTimeHour - 1) {
                    tempScaleWidth = mScaleWidth * 2f;
                    tempTimeTextSize = mTimeTextSize * 2f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + mNormalGap * mScale[1], getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + mNormalGap * mScale[1], mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + mNormalGap * mScale[1] + tempTimeTextSize / 2, mTimeTextPaint);
                } else if (i == mTimeHour) {
                    tempScaleWidth = mScaleWidth * 2.5f;
                    tempTimeTextSize = mTimeTextSize * 2.5f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[2])), getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[2])), mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[2])) + tempTimeTextSize / 2, mTimeTextPaint);
                } else if (i == mTimeHour + 1) {
                    tempScaleWidth = mScaleWidth * 2f;
                    tempTimeTextSize = mTimeTextSize * 2f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[2] * 2)), getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[2] * 2)), mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] + mScale[2] * 2)) + tempTimeTextSize / 2, mTimeTextPaint);
                } else if (i == mTimeHour + 2) {
                    tempScaleWidth = mScaleWidth * 1.5f;
                    tempTimeTextSize = mTimeTextSize * 1.5f;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] * 2 + mScale[2] * 2)), getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] * 2 + mScale[2] * 2)), mLinePaint);
                    canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] * 2+ mScale[2] * 2)) + tempTimeTextSize / 2, mTimeTextPaint);
                } else {
                    tempScaleWidth = mScaleWidth;
                    tempTimeTextSize = mTimeTextSize;
                    mTimeTextPaint.setTextSize(tempTimeTextSize);
                    canvas.drawLine(getX(), getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] * 2 + mScale[0] + mScale[2] * 2)), getX() + tempScaleWidth, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] * 2 + mScale[0] + mScale[2] * 2)), mLinePaint);
                    if (i % 6 == 0) {
                        canvas.drawText(timeText[i], getX() + tempScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * scaleGap + (mNormalGap * (mScale[1] * 2 + mScale[0] + mScale[2] * 2)) + tempTimeTextSize / 2, mTimeTextPaint);
                    }
                }
            }
        }*/
    }
}

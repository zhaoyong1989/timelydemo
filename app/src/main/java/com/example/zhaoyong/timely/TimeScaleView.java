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

/**
 * Created by zhaoyong on 17/8/31.
 */

public class TimeScaleView extends View {

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

    public TimeScaleView(Context context) {
        this(context, null);
        Log.i(TAG, "TimeScaleView1");
    }

    public TimeScaleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        Log.i(TAG, "TimeScaleView2");
    }

    public TimeScaleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        Log.i(TAG, "TimeScaleView3");
    }

    public TimeScaleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
    private float caculateScaleGap() {
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
    }

    /**
     * 绘制未放大时视图
     * @param canvas
     */
    private void drawNormalScale(Canvas canvas) {
        mTimeTextPaint.setTextSize(mTimeTextSize);
        for(int i=0; i<timeText.length; i++) {
            canvas.drawLine(getX(), getY() + mTopPadding + i * mNormalGap, getX() + mScaleWidth, getY() + mTopPadding + i * mNormalGap, mLinePaint);
            if(i % 6 == 0) {
                canvas.drawText(timeText[i], getX() + mScaleWidth + mPaddingTimeWithScale, getY() + mTopPadding + i * mNormalGap + mTimeTextSize / 2, mTimeTextPaint);
            }
        }
    }

    /**
     * 绘制放大时的视图
     * @param canvas
     */
    private void drawLargeScale(Canvas canvas) {
        float scaleGap = caculateScaleGap();
        float tempScaleWidth;
        float tempTimeTextSize;

        Log.i(TAG, "scaleGap:" + scaleGap + " time:" + mTimeHour + " normalGap:" + mNormalGap);

        /**
         * 实现有些复杂，冗余，耦合性高，后面有待完善算法，通过创建每个刻度的坐标数组数据结构解耦
         * 将计算和绘制分开
         */
        for(int i=0; i<timeText.length; i++) {
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
        }
    }
}

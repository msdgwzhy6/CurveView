package com.sctdroid.app.curveview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lixindong on 9/23/16.
 */

public class CurveView extends View {
    public CurveView(Context context) {
        this(context, null);
    }

    public CurveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CurveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        init();
    }

    public static final String TAG = CurveView.class.getSimpleName();

    protected int mPairDataKeysId;
    protected int mPairDataValuesId;
    protected int mUnitWidth;
    protected int mFillColor;
    protected int mContentColor;
    protected int mStrokeWidth;
    protected int mContentPaddingTop;
    protected int mContentPaddingBottom;
    protected int mDotTextSize;
    protected int mDotTextColor;
    protected int mAxisTextSize;
    protected int mAxisTextColor;

    private int mCorner;

    protected int mContentPaddingStart;
    protected int mContentPaddingEnd;

    protected int mGravity = 0;

    private boolean mShowAll = false;

    /**
     <flag name="top" value="0x01" />
     <flag name="bottom" value="0x02" />
     <flag name="start" value="0x04" />
     <flag name="end" value="0x08" />
     <flag name="center_vertical" value="0x10" />
     <flag name="center_horizontal" value="0x20" />
     <flag name="center" value="0x30" />
     */
    public final int GRAVITY_TOP = 0x01;
    public final int GRAVITY_BOTTOM = 0x02;
    public final int GRAVITY_START = 0x04;
    public final int GRAVITY_END = 0x08;
    public final int GRAVITY_CENTER_VERTICAL = 0x10;
    public final int GRAVITY_CENTER_HORIZONTAL = 0x20;
    public final int GRAVITY_CENTER = 0x30;

    private boolean mShowXLine = false;
    private boolean mShowXText = false;
    private boolean mShowY = false;

    protected List<Pair<String, Integer>> mPairData;

    protected Paint mContentPaint;
    protected Paint mBackgroundPaint;
    protected Paint mFirstPaint;
    protected TextPaint mSecondPaint;
    private Drawable mBackground;

    protected int mOffsetX = 0;

    protected Path mContentPath;
    protected CornerPathEffect mCornerPathEffect;

    private void init() {
        mCornerPathEffect = new CornerPathEffect(mCorner);

        mContentPaint = new Paint();
        mContentPaint.setStyle(Paint.Style.STROKE);
        mContentPaint.setColor(mContentColor);
        mContentPaint.setStrokeWidth(mStrokeWidth);
        mContentPaint.setPathEffect(mCornerPathEffect);

        mBackgroundPaint = new Paint();
//        mBackgroundPaint.setColor(mFillColor);

        mFirstPaint = new Paint();
        mFirstPaint.setColor(mAxisTextColor);
        mFirstPaint.setTextSize(mAxisTextSize);

        mSecondPaint = new TextPaint();
        mSecondPaint.setColor(mDotTextColor);
        mSecondPaint.setTextSize(mDotTextSize);

        mContentPath = new Path();
        //        mMaxVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
        mMaxVelocity = 3000;
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Curve, 0, 0);
        try {
            mUnitWidth = a.getDimensionPixelSize(R.styleable.Curve_unitWidth, 120);
            mFillColor = a.getColor(R.styleable.Curve_backgroundColor, Color.WHITE);
            mContentColor = a.getColor(R.styleable.Curve_contentColor, Color.BLACK);
            mStrokeWidth = a.getDimensionPixelSize(R.styleable.Curve_strokeWidth, 10);
            mContentPaddingTop = a.getDimensionPixelSize(R.styleable.Curve_contentPaddingTop, 0);
            mContentPaddingBottom = a.getDimensionPixelSize(R.styleable.Curve_contentPaddingBottom, 0);
            mDotTextSize = a.getDimensionPixelSize(R.styleable.Curve_dotTextSize, 60);
            mDotTextColor = a.getColor(R.styleable.Curve_dotTextColor, Color.BLACK);
            mAxisTextSize = a.getDimensionPixelSize(R.styleable.Curve_axisTextSize, 40);
            mAxisTextColor = a.getColor(R.styleable.Curve_axisTextColor, Color.BLACK);

            mCorner = a.getDimensionPixelSize(R.styleable.Curve_corner, 0);

            mContentPaddingStart = a.getDimensionPixelSize(R.styleable.Curve_contentPaddingStart, 0);
            mContentPaddingEnd = a.getDimensionPixelSize(R.styleable.Curve_contentPaddingEnd, 0);

            mShowXLine = a.getBoolean(R.styleable.Curve_showXLine, false);
            mShowXText = a.getBoolean(R.styleable.Curve_showXText, false);
            mShowY = a.getBoolean(R.styleable.Curve_showY, false);

            mGravity = a.getInteger(R.styleable.Curve_dotTextGravity, 0);

            mShowAll = a.getBoolean(R.styleable.Curve_showAll, false);

            mPairData = new ArrayList<>();

            int[] values = getResources().getIntArray(R.array.values);
            String[] keys = getResources().getStringArray(R.array.keys);

            for (int i = 0; i < values.length && i < keys.length; i++) {
                Pair<String, Integer> pair = new Pair<>(keys[i], values[i]);
                mPairData.add(pair);
            }
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float scaleX = 1f;
        int unitWidth = mUnitWidth;
        if (mShowAll) {
            unitWidth = (getWidth() - mContentPaddingStart - mContentPaddingEnd) / (mPairData.size() - 1);
        }

        if (mContentPath.isEmpty()) {
            int height = getHeight();
            int rateY = (height - mContentPaddingTop - mContentPaddingBottom) / 100;
            mContentPath.moveTo(0, height - mContentPaddingBottom - mPairData.get(0).second * rateY);
            for (int i = 1; i < mPairData.size(); i++) {
                mContentPath.lineTo(i * unitWidth * scaleX, height - mContentPaddingBottom - mPairData.get(i).second * rateY);
            }
        }

        canvas.save();
        canvas.translate(mOffsetX + mContentPaddingStart, 0);

        canvas.drawColor(mFillColor);
        canvas.drawPath(mContentPath, mContentPaint);

        int rateY = (getHeight() - mContentPaddingTop - mContentPaddingBottom) / 100;
        for (int i = 0; i < mPairData.size(); i++) {
            int bottomY = getHeight() - mContentPaddingBottom;
            int dotX = (int) (unitWidth * scaleX * i);
            int dotY = bottomY - mPairData.get(i).second * rateY;
            if (mShowXText) {
                canvas.drawText(mPairData.get(i).first, dotX, bottomY + mAxisTextSize, mFirstPaint);
            }
            String dotText = mPairData.get(i).second + "";
            int offsetX = getTextOffsetX(mSecondPaint, dotText, mGravity);
            int offsetY = getTextOffsetY(mSecondPaint, mGravity);

            canvas.drawText(dotText,dotX + offsetX, dotY + offsetY, mSecondPaint);
        }

        canvas.restore();
        if (mShowXLine) {
            canvas.drawLine(0, getHeight() - mContentPaddingBottom, getWidth(), getHeight() - mContentPaddingBottom, mContentPaint);
        }
    }

    private int getTextOffsetY(TextPaint paint, int gravity) {
        int height = (int) (paint.getFontMetrics().descent - paint.getFontMetrics().ascent);
        int offset = (int) (paint.getFontMetrics().descent + paint.getFontMetrics().ascent) / 2;
        if ((gravity & GRAVITY_CENTER_VERTICAL) != 0) {
            offset += height / 2;
        } else if ((gravity & GRAVITY_BOTTOM) != 0) {
            offset += height;
        }
        return offset;
    }

    private int getTextOffsetX(TextPaint paint, String s, int gravity) {
        int width = (int) paint.measureText(s);
        int offset = 0;
        if ((gravity & GRAVITY_CENTER_HORIZONTAL) != 0) {
            offset = - width / 2;
        } else if ((gravity & GRAVITY_START) != 0) {
            offset = - width;
        }

        return offset;
    }

    public void setOffsetX(int offsetX) {
        this.mOffsetX = offsetX;
    }

    int mLastX;
    VelocityTracker mVelocityTracker;
    int mMaxVelocity;

    private void acquireVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mShowAll) {
            return false;
        }
        acquireVelocityTracker(event);
        final VelocityTracker velocityTracker = mVelocityTracker;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                int offset = (int) (mOffsetX + (event.getRawX() - mLastX));
                offset = checkOffset(offset);
                setOffsetX(offset);
                mLastX = (int) event.getRawX();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                final float velocityX = velocityTracker.getXVelocity();
                final int initialOffset = mOffsetX;
                ValueAnimator animator = ValueAnimator.ofFloat(velocityX, 0);
                final int duration = 300;
                animator.setDuration(duration);
                animator.setInterpolator(new DecelerateInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float v = (Float) animation.getAnimatedValue();
                        int t = (int) animation.getCurrentPlayTime();
                        int d = (int) (velocityX * duration * 1.0f / 1000 / 2 - v * (duration - t) * 1.0f / 1000 / 2);
                        Log.d(TAG, "velocity X " + animation.getAnimatedValue());
                        Log.d(TAG, "d " + d);
                        setOffsetX(checkOffset(initialOffset + d));
                        invalidate();
                    }
                });
                animator.start();
                break;
        }
        return true;
    }

    /**
     * offset > 0, scroll to left
     * offset < 0, scroll to right
     * normally, offset should <= 0
     * @param offset offset to scroll horizontally
     * @return fixed offset, not to exceed limit
     */
    private int checkOffset(int offset) {
        // only scroll when paint width > view width
        int paintWdith = (mPairData.size() - 1) * mUnitWidth + mContentPaddingStart + mContentPaddingEnd;
        if (paintWdith < getWidth()) {
            return mOffsetX;
        }

        if (offset > 0) {
            offset = 0;
        }
        if (offset < getWidth() - paintWdith) {
            offset = getWidth() - paintWdith;
        }
        return offset;
    }
}

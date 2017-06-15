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
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lixindong on 9/23/16.
 */

public class CurveView extends View implements DataObserver {
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
    public final static int GRAVITY_TOP = 0x01;
    public final static int GRAVITY_BOTTOM = 0x02;
    public final static int GRAVITY_START = 0x04;
    public final static int GRAVITY_END = 0x08;
    public final static int GRAVITY_CENTER_VERTICAL = 0x10;
    public final static int GRAVITY_CENTER_HORIZONTAL = 0x20;
    public final static int GRAVITY_CENTER = 0x30;

    private boolean mShowXLine = false;
    private boolean mShowXText = false;
    private boolean mShowY = false;

    protected Paint mContentPaint;
    protected Paint mBackgroundPaint;
    protected TextPaint mXAxisPaint;
    protected TextPaint mDotTextPaint;
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

        mXAxisPaint = new TextPaint();
        mXAxisPaint.setColor(mAxisTextColor);
        mXAxisPaint.setTextSize(mAxisTextSize);

        mDotTextPaint = new TextPaint();
        mDotTextPaint.setColor(mDotTextColor);
        mDotTextPaint.setTextSize(mDotTextSize);

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

        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(mFillColor);

        if (mDecorations == null || mDecorations.size() == 0) {
            return;
        }

        float scaleX = 1f;
        int unitWidth = mUnitWidth;
        if (mShowAll) {
            unitWidth = (getWidth() - mContentPaddingStart - mContentPaddingEnd) / (mDecorations.size() - 1);
        }

        if (mContentPath.isEmpty() || mForceUpdate) {
            mForceUpdate = false;
            int height = getHeight();
            int heightPerLevel = (height - mContentPaddingTop - mContentPaddingBottom) / (mMaxLevel - mMinLevel);

            mContentPath.moveTo(0, height - mContentPaddingBottom - (mDecorations.get(0).mLevel - mMinLevel) * heightPerLevel);
            for (int i = 1; i < mDecorations.size(); i++) {
                mContentPath.lineTo(i * unitWidth * scaleX, height - mContentPaddingBottom - (mDecorations.get(i).mLevel - mMinLevel) * heightPerLevel);
            }
        }

        canvas.save();
        canvas.translate(mOffsetX + mContentPaddingStart, 0);

        canvas.drawPath(mContentPath, mContentPaint);

        int heightPerLevel = (getHeight() - mContentPaddingTop - mContentPaddingBottom) / (mMaxLevel - mMinLevel);
        for (int i = 0; i < mDecorations.size(); i++) {
            ItemDecoration decoration = mDecorations.get(i);

            int bottomY = getHeight() - mContentPaddingBottom;
            int dotX = (int) (unitWidth * scaleX * i);
            int dotY = bottomY - (mDecorations.get(i).mLevel - mMinLevel) * heightPerLevel;
            if (mShowXText) {
                int offsetX = getTextOffsetX(mXAxisPaint, decoration.mXAxisText, GRAVITY_CENTER_HORIZONTAL);
                canvas.drawText(decoration.mXAxisText, dotX + offsetX, bottomY + mAxisTextSize, mXAxisPaint);
            }
            for (Mark mark : decoration.mMarks) {
                int offsetX = getTextOffsetX(mDotTextPaint, mark.content, mark.gravity) + mark.marginStart - mark.marginEnd;
                int offsetY = getTextOffsetY(mDotTextPaint, mark.gravity) + mark.marginTop - mark.marginBottom;

                canvas.drawText(mark.content, dotX + offsetX, dotY + offsetY, mDotTextPaint);
            }
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
        int paintWdith = (mDecorations.size() - 1) * mUnitWidth + mContentPaddingStart + mContentPaddingEnd;
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

    private Adapter mAdapter;

    public void setAdapter(Adapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(this);
        }
        mAdapter = adapter;
        adapter.registerDataSetObserver(this);

        updateAdapterData();
    }

    @Override
    public void onChanged() {
        updateAdapterData();
    }

    private int mMinLevel = 0;
    private int mMaxLevel = 100;
    private SparseArray<ItemDecoration> mDecorations = new SparseArray<>();

    private boolean mForceUpdate = false;


    private void updateAdapterData() {
        mForceUpdate = true;
        clearData();
        if (mAdapter == null) {
            return;
        }

        mMinLevel = mAdapter.getMinLevel();
        mMaxLevel = mAdapter.getMaxLevel();

        for (int i = 0; i < mAdapter.getCount(); i++) {
            ItemDecoration decoration = new ItemDecoration();

            int level = mAdapter.getLevel(i);
            Set<Mark> marks = mAdapter.onCreateMarks(i);
            String xAxisText = mAdapter.getXAxisText(i);

            decoration.mLevel = level;
            decoration.mMarks = marks;
            decoration.mXAxisText = xAxisText;

            mAdapter.decorate(decoration, i);
            mDecorations.append(i, decoration);
        }
    }

    private void clearData() {
        // dot data
        // dot text data
        mDecorations.clear();
        // line data
        // other data
    }

    public abstract static class Adapter {

        private final DataObservable mDataSetObservable = new DataObservable();

        public void registerDataSetObserver(DataObserver observer) {
            mDataSetObservable.registerObserver(observer);
        }

        public void unregisterDataSetObserver(DataObserver observer) {
            mDataSetObservable.unregisterObserver(observer);
        }

        /**
         * Notifies the attached observers that the underlying data has been changed
         * and any View reflecting the data set should refresh itself.
         */
        public void notifyDataSetChanged() {
            mDataSetObservable.notifyChanged();
        }

        public abstract int getCount();

        public void draw(Canvas canvas, int x, int y, int position) {}

        public void decorate(ItemDecoration decoration, int position) {}

        public Set<Mark> onCreateMarks(int position) {
            return Collections.emptySet();
        }

        public int getMinLevel() {
            return 0;
        }

        public int getMaxLevel() {
            return 100;
        }

        public abstract int getLevel(int position);

        public String getXAxisText(int i) {
            return "";
        }

    }

    public static class ItemDecoration {
        protected Set<Mark> mMarks = new HashSet<>();
        protected int mLevel;
        protected String mXAxisText;
    }

    public static class Mark {
        public final String content;
        public final int gravity;
        public final int marginStart;
        public final int marginEnd;
        public final int marginTop;
        public final int marginBottom;
        public final TextAppearanceSpan textAppearanceSpan;

        public Mark(String content) {
            this(content, GRAVITY_BOTTOM | GRAVITY_CENTER_HORIZONTAL);
        }

        public Mark(String content, int gravity) {
            this(content, gravity, 0, 0, 0, 0);
        }

        public Mark(String content, int gravity, int marginStart, int marginTop, int marginEnd, int marginBottom) {
            this(content, gravity, marginStart, marginTop, marginEnd, marginBottom, null);
        }

        public Mark(String content, int gravity, int marginStart, int marginTop, int marginEnd, int marginBottom, TextAppearanceSpan mTextAppearanceSpan) {
            this.content = content;
            this.gravity = gravity;
            this.marginStart = marginStart;
            this.marginEnd = marginEnd;
            this.marginTop = marginTop;
            this.marginBottom = marginBottom;
            this.textAppearanceSpan = mTextAppearanceSpan;
        }
    }
}

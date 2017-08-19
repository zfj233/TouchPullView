package com.zfj.android.touchpulldemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

/**
 * Created by zfj_ on 2017/8/5.
 */

public class TouchPullView extends View {
    private Paint mCirclePaint;
    private float mCircleRadius = 50;
    private float mCircleX, mCircleY;
    private float mProgress;
    private int mDragHeight = 400;
    //目标宽度
    private int mTargetWidth = 400;
    //重心点最终高度，决定控制点Y坐标
    private int mTargetGravityHeight = 10;
    private int mTangentAngle = 110;
    //绘制路径画笔及路径
    private Paint mPathPaint;
    private Path mPath = new Path();
    private ValueAnimator mValueAnimator;
    private Interpolator mProgressInterpolator = new DecelerateInterpolator();
    private Interpolator mTangentAngleInterpolator;
    private Drawable mContext = null;
    private int mContentMargin = 0;

    public TouchPullView(Context context) {
        super(context);
        init(null);
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TouchPullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TouchPullView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    /**
     * 初始化操作
     */
    private void init(AttributeSet attrs) {

        //画圆画笔
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setAntiAlias(true);
        p.setDither(true);
        p.setColor(0xff000000);
        p.setStyle(Paint.Style.FILL);
        mCirclePaint = p;

        //路径画笔
        p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setAntiAlias(true);
        p.setDither(true);
        p.setColor(0xff000000);
        p.setStyle(Paint.Style.FILL);
        mPathPaint = p;
        //切角路径插值器
        mTangentAngleInterpolator = PathInterpolatorCompat.create(
                (mCircleRadius * 2.0f) / mDragHeight,
                90.0f / mTangentAngle
        );

        final Context context = getContext();
        //获取属性
        TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.TouchPullView, 0, 0);
        int color = array.getColor(R.styleable.TouchPullView_pColor, 0x20000000);
        mCircleRadius = array.getDimension(R.styleable.TouchPullView_pRadius, mCircleRadius);
        mDragHeight = array.getDimensionPixelOffset(R.styleable.TouchPullView_pDragHeight, mDragHeight);
        mTangentAngle = array.getInteger(R.styleable.TouchPullView_pTangentAngle, 100);
        mTargetWidth = array.getDimensionPixelOffset(R.styleable.TouchPullView_pTargetWidth, mTargetWidth);
        mTargetGravityHeight = array.getDimensionPixelSize(R.styleable.TouchPullView_pTargetGravityHeight,
                mTargetGravityHeight);
        mContext = array.getDrawable(R.styleable.TouchPullView_pContentDrawable);
        mContentMargin = array.getDimensionPixelSize(R.styleable.TouchPullView_pContentDrawableMargin, 0);
        //销毁
        array.recycle();
    }

    /**
     * 尺寸改变时调用
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        requestPathLayout();
    }

    /**
     * 更新路径相关操作
     */
    private void requestPathLayout() {
        //获取进度
        final float progress = mProgressInterpolator.getInterpolation(mProgress);
        //获取可绘制区域高度
        final float w = getValueByLine(getWidth(), mTargetWidth, mProgress);
        final float h = getValueByLine(0, mDragHeight, mProgress);
        //x对称轴参数 圆心x
        final float cPointX = w / 2.0f;
        //圆的半径
        final float cRadius = mCircleRadius;
        //圆的Y
        final float cPointY = h - cRadius;
        //控制点结束的Y
        final float endControlY = mTargetGravityHeight;
        //更新圆心坐标
        mCircleX = cPointX;
        mCircleY = cPointY;
        //重置
        final Path path = mPath;
        path.reset();
        path.moveTo(0, 0);
        //控制点，结束点对应的X,Y坐标
        float lControlX;
        float lControlY;
        float lEndX;
        float lEndY;
        //角度
        float angle = mTangentAngle * mTangentAngleInterpolator.getInterpolation(progress);
        double radian = Math.toRadians(getValueByLine(0, angle, progress));
        lEndX = (float) (cPointX - Math.sin(radian) * cRadius);
        lEndY = (float) (cPointY + Math.cos(radian) * cRadius);
        lControlY = getValueByLine(0, endControlY, progress);
        float tHeight = lEndY - lControlY;
        lControlX = (float) (lEndX - tHeight / Math.tan(radian));
        path.quadTo(lControlX, lControlY, lEndX, lEndY);

        //右侧
        path.lineTo(cPointX + (cPointX - lEndX), lEndY);
        path.quadTo(cPointX + cPointX - lControlX, lControlY, w, 0);

        //更新内容Drawable
        updateContentLayout(cPointX, cPointY, cRadius);
    }

    /**
     * 获取当前值
     *
     * @param start
     * @param end
     * @param progress
     * @return
     */
    private float getValueByLine(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int count = canvas.save();
        float transX = (getWidth() - getValueByLine(getWidth(), mTargetWidth, mProgress)) / 2;
        canvas.translate(transX, 0);
        canvas.drawCircle(mCircleX, mCircleY, mCircleRadius, mCirclePaint);
        canvas.drawPath(mPath, mPathPaint);

        Drawable drawable = mContext;
        if (drawable != null) {
            //剪切矩形区域
            canvas.clipRect(drawable.getBounds());
            //绘制Drawable
            drawable.draw(canvas);
            canvas.restore();
        }
        canvas.restoreToCount(count);
    }

    /**
     * 测量View方法
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //最大宽度
        int iWidth = (int) ((2 * mCircleRadius) + getPaddingRight() + getPaddingLeft());
        //最大高度
        int iHeight = (int) ((mDragHeight * mProgress + 0.5f)
                + getPaddingTop() + getPaddingBottom());
        //测量高度，宽度
        int measureWidth;
        int measureHeight;
        //获取设置宽度
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            measureWidth = width;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            measureWidth = Math.min(iWidth, width);
        } else {
            measureWidth = iWidth;
        }
        //获取设置高度
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            measureHeight = height;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            measureHeight = Math.min(iHeight, width);
        } else {
            measureHeight = iHeight;
        }

        setMeasuredDimension(measureWidth, measureHeight);
    }

    /**
     * 释放操作
     */
    public void release() {
        if (mValueAnimator == null) {
            ValueAnimator animator = ValueAnimator.ofFloat(mProgress, 0f);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(400);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Object val = animation.getAnimatedValue();
                    if (val instanceof Float) {
                        setProgress((Float) val);
                    }
                }
            });
            mValueAnimator = animator;
        } else {
            mValueAnimator.cancel();
            mValueAnimator.setFloatValues(mProgress, 0);
        }
        mValueAnimator.start();
    }

    //设置滑动进度
    public void setProgress(float progress) {
        mProgress = progress;
        //重置布局
        requestLayout();
    }

    /**测量内容Drawable
     * @param cx
     * @param cy
     * @param radius
     */
    private void updateContentLayout(float cx, float cy, float radius) {
        Drawable drawable = mContext;
        if (drawable != null) {
            int margin = mContentMargin;
            int l = (int) (cx - radius + margin);
            int r = (int) (cx + radius - margin);
            int t = (int) (cy - radius + margin);
            int b = (int) (cy + radius - margin);
            drawable.setBounds(l,t,r,b);
        }
    }
}

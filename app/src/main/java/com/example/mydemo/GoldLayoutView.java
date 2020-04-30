package com.example.mydemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GoldLayoutView extends FrameLayout {

    private int parentWidth;
    private int parentHeight;

    private int addChildDefHeight = 0;
    private int addChildDefWidth = 0;

    private int maxChildNumber = 7;
    private int childSpacing = 0;
    private int childSize = 0;

    private int marginTop;
    private int upDownMoveHeight;

    private int scaleFactor = 6;

    private List<Float> childXList = new ArrayList<>();

    private List<Float> childXList2 = new ArrayList<>();

    // 上升动画
    private ValueAnimator riseAnim;
    // 上下
    private ValueAnimator upDownAnim;
    // 弹出动画
    private ValueAnimator popUpAnim;
    // 移除动画
    private ValueAnimator removeAnim;
    // 填充动画
    private ValueAnimator fillAnim;

    private int popUpHeight;

    private boolean lastChildUpDownMove = false;

    boolean isFill = false;
    int last2Spacing = 0;

    private OnItemClickListener mItemClickListener;

    public GoldLayoutView(Context context) {
        this(context, null);
    }

    public GoldLayoutView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GoldLayoutView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        post(new Runnable() {
            @Override
            public void run() {
                parentWidth = getWidth();
                parentHeight = getHeight();
            }
        });

        initData();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    private void initData() {
        addChildDefHeight = dp2px(200);
        addChildDefWidth = dip2px(16);

        popUpHeight = dp2px(48);

        childSpacing = dp2px(16);
        marginTop = dp2px(32);
        upDownMoveHeight = dp2px(4);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }


    public void addGoldChildView(final int goldNumber) {
        if (parentHeight == 0 || parentWidth == 0) {
            return;
        }

        if (riseAnim != null && riseAnim.isRunning()) {
            return;
        }

        if (popUpAnim != null && popUpAnim.isRunning()) {
            return;
        }

        if (removeAnim != null && removeAnim.isRunning()) {
            return;
        }

        if (fillAnim != null && fillAnim.isRunning()) {
            return;
        }


        // 计算单个child大小
        childSize = (parentWidth - maxChildNumber * childSpacing) / (maxChildNumber - 1);


//        ImageView view = new ImageView(getContext());
//        view.setLayoutParams(new FrameLayout.LayoutParams(childSize, childSize));
//        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//        view.setImageResource(R.mipmap.icon_top_gold);

        final View child = getChildView(childSize, goldNumber);

        child.setX(addChildDefWidth);
        child.setY(addChildDefHeight);
        addView(child);

        // indexOfChild(child)
        child.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onClick(indexOfChild(child), goldNumber);
                }
            }
        });

        // 弹出动画
        startPopUpAnim();

    }


    // 开启弹出动画
    private void startPopUpAnim() {
        popUpAnim = ValueAnimator.ofFloat(0f, 1.0f);
        popUpAnim.setDuration(500);
        popUpAnim.setInterpolator(new DecelerateInterpolator());
        popUpAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                View lastChild = getChildAt(getChildCount() - 1);
                lastChild.setScaleX(1.2f * value);
                lastChild.setScaleY(1.2f * value);

                int offset = (int) (value * popUpHeight);
                lastChild.setY(addChildDefHeight - offset);
            }
        });
        popUpAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 开启上升动画
                startRiseAnim();
            }
        });
        popUpAnim.start();
    }

    public void startRemoveAnim(final int childIndex) {
        if (removeAnim != null && removeAnim.isRunning()) {
            return;
        }
        if (childIndex < 0 || childIndex >= getChildCount()) {
            return;
        }

        childXList2 = new ArrayList<>();

        for (int i = 0; i < getChildCount(); i++) {
            childXList2.add(getChildAt(i).getX());
        }

        removeAnim = ValueAnimator.ofFloat(0f, 1.0f);
        removeAnim.setInterpolator(new LinearInterpolator());
        removeAnim.setDuration(1000);
        removeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                int offset = (int) ((childSize + childSpacing) * value);
                int childCount = getChildCount();
                for (int i = childIndex; i < childCount; i++) {
                    View childView = getChildAt(i);
                    if (i == childIndex) {
                        childView.setScaleX(1.0f + 0.5f * value * scaleFactor);
                        childView.setScaleY(1.0f + 0.5f * value * scaleFactor);
                        childView.setAlpha(1.0f - value * scaleFactor);
                    } else {
                        childView.setX(childXList2.get(i) + offset);
                    }
                }
            }
        });
        removeAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                removeViewAt(childIndex);
            }
        });
        removeAnim.start();
    }

    private FrameLayout getChildView(int size, int goldNumber) {
        FrameLayout layout = new FrameLayout(getContext());
        layout.setLayoutParams(new FrameLayout.LayoutParams(size, size));

        ImageView ivBg = new ImageView(getContext());
        ivBg.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ivBg.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ivBg.setImageResource(R.mipmap.icon_top_gold);

        layout.addView(ivBg);

        TextView goldView = new TextView(getContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        lp.bottomMargin = dp2px(4);
        goldView.setTextSize(15);
        goldView.setGravity(Gravity.CENTER);
        goldView.setTextColor(Color.parseColor("#FFFFFF"));
        goldView.getPaint().setFakeBoldText(true);
        goldView.setText("+" + goldNumber);
        goldView.setLayoutParams(lp);
        goldView.setShadowLayer(2, 4, 4, Color.parseColor("#FE6100"));

        layout.addView(goldView);

        return layout;
    }

    private void startRiseAnim() {
        if (riseAnim != null && riseAnim.isRunning()) {
            return;
        }

        childXList = new ArrayList<>();

        isFill = false;
        last2Spacing = 0;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            childXList.add(getChildAt(i).getX());
        }

        if (childCount > 1) {
            View lastView = getChildAt(childCount - 1);
            View last2View = getChildAt(childCount - 2);

            // isFill = lastView.getX() != last2View.getX();
            // last2Spacing = (int) (last2View.getX() - lastView.getX());

            int space = (int) (last2View.getX() - lastView.getX());
            isFill = (space + 2) > (childSize + childSpacing); // +2 精度处理
            last2Spacing = space - (childSize + childSpacing);
        }

        riseAnim = ValueAnimator.ofFloat(0f, 1.0f);
        riseAnim.setInterpolator(new LinearInterpolator());
        riseAnim.setDuration(1000);
        riseAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                int y = (int) (addChildDefHeight - popUpHeight - (addChildDefHeight - marginTop - popUpHeight) * value);

                int offset = (int) ((childSize + childSpacing) * value);


                if (getChildCount() > 0) {

                    for (int i = 0; i < getChildCount(); i++) {
                        View childView = getChildAt(i);
                        if (i == (getChildCount() - 1)) {
                            childView.setY(y);

                            // 设置缩放
                            childView.setScaleX(1.2f - 0.2f * value);
                            childView.setScaleY(1.2f - 0.2f * value);

                        } else {

                            if (i == 0 && getChildCount() == maxChildNumber) {

                                childView.setScaleX(1.0f + 0.5f * value * scaleFactor);
                                childView.setScaleY(1.0f + 0.5f * value * scaleFactor);

                                childView.setAlpha(1.0f - value * scaleFactor);

                            } else {

                                if (!isFill) {
                                    childView.setX(childXList.get(i) + offset);
                                }

                            }

                        }
                    }

                }

            }
        });

        riseAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                removeOverChild();
                lastChildUpDownMove = true;

                if (isFill && last2Spacing != 0) {
                    startFillAnim(last2Spacing);
                }

                // 开启上下移动
                startUpDownAnim();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                lastChildUpDownMove = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                removeOverChild();
            }
        });
        riseAnim.start();
    }

    /**
     * @param spacing
     */
    private void startFillAnim(int spacing) {
        final View lastView = getChildAt(getChildCount() - 1);
        int startX = (int) lastView.getX();
        fillAnim = ValueAnimator.ofInt(startX, startX + spacing);
        fillAnim.setDuration(1000);
        fillAnim.setInterpolator(new LinearInterpolator());
        fillAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int x = (int) animation.getAnimatedValue();
                lastView.setX(x);
            }
        });
        fillAnim.start();
    }

    private void removeOverChild() {
        int childCount = getChildCount();
        if (childCount >= maxChildNumber) {
            for (int i = 0; i <= (childCount - maxChildNumber); i++) {
                removeViewAt(i);
            }
        }
    }


    private void startUpDownAnim() {
        if (upDownAnim != null && upDownAnim.isRunning()) {
            return;
        }

        upDownAnim = ValueAnimator.ofFloat(0f, 1.0f, 0f, -1f, 0f);
        upDownAnim.setDuration(2000);
        upDownAnim.setInterpolator(new LinearInterpolator());
        upDownAnim.setRepeatCount(-1);
        upDownAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                int childCount = getChildCount();

                for (int i = 0; i < childCount; i++) {
                    int offset = (int) (value * upDownMoveHeight);
                    View childView = getChildAt(i);
                    if (i == (childCount - 1)) {
                        if (lastChildUpDownMove) {
                            setUpDownY(i, offset, childView);
                        }
                    } else {
                        setUpDownY(i, offset, childView);
                    }
                }

            }
        });
        upDownAnim.start();
    }

    private void setUpDownY(int i, int offset, View childView) {
        if (i % 2 == 0) {
            childView.setY(marginTop + offset);
        } else {
            childView.setY(marginTop - offset);
        }
    }

    /**
     * onPause todo 必须调用
     */
    public void onPause() {
        if (upDownAnim != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                upDownAnim.pause();
            }
        }
    }

    /**
     * onPause todo 必须调用
     */
    public void onResume() {
        if (upDownAnim != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                upDownAnim.resume();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (upDownAnim != null) {
            upDownAnim.end();
            upDownAnim = null;
        }
    }

    /**
     * dip转px
     *
     * @param dpValue dp值
     * @return px值
     */
    public int dip2px(float dpValue) {
        return dp2px(dpValue);
    }

    /**
     * dp转px
     *
     * @param dpValue dp值
     * @return px值
     */
    public int dp2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public interface OnItemClickListener {
        void onClick(int childIndex, int goldNumber);
    }

}

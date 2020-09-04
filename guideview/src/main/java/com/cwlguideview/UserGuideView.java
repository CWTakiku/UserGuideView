package com.cwlguideview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


import com.cwlguideview.config.Config;
import com.cwlguideview.config.Direction;
import com.cwlguideview.config.HighLightStyle;
import com.cwlguideview.config.MaskBlurStyle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * create by chengwl
 * this is a common userGuideView
 * support four kinds of highLight style and one guide can show multiple highLightView
 * you can set different tip view  String or Drawable and you can also set offset that you want
 */
public class UserGuideView extends View {


    private Bitmap maskBitmap;// 前景
    private Canvas mCanvas;// 绘制蒙版层的画布
    private Paint mPaint;// 绘制蒙版层画笔
    private Paint mORIGINALPaint;
    private boolean touchOutsideCancel = false; // 外部关闭当前引导
    private float offsetPadding;
    private float borderWidth;
    private UserGuideListener onUserGuideListener;
    private int statusBarHeight;// 状态栏高度
    private List<HighLightView> currentGuide;//当前的引导视图

    private Rect tipViewHitRect;
    private LinkedHashMap<Bitmap, Rect> alwaysShowViews;
    private int screenH, screenW;
    private List<List<HighLightView>> lists = new ArrayList<>();
    private Config config;

    public UserGuideView(Context context) {
        this(context, null);
    }

    public UserGuideView(Context context, AttributeSet set) {
        this(context, set, -1);
    }

    public UserGuideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.UserGuideView);
            int highLightStyle = array.getInt(R.styleable.UserGuideView_HighlightViewStyle, getConfig().getHighLightStyle());
            int maskblurstyle = array.getInt(R.styleable.UserGuideView_MaskBlurStyle, getConfig().getMaskBlurStyle());
            int maskColor = array.getColor(R.styleable.UserGuideView_maskColor, getConfig().getMaskColor());
            float offsetMargin = array.getDimension(R.styleable.UserGuideView_offsetMargin, getConfig().getOffsetPadding());
            getConfig().setOffsetPadding(offsetMargin)
                    .setMaskColor(maskColor)
                    .setMaskBlurStyle(maskblurstyle)
                    .setHighLightStyle(highLightStyle);
            array.recycle();
        }
        // 初始化对象
        init(context);
    }


    /**
     * 初始化对象
     */
    private void init(Context context) {
        //禁用硬件加速 否则在一些机型上 会显示不出来
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        mPaint.setARGB(0, 255, 0, 0);
        mORIGINALPaint = new Paint();
        BlurMaskFilter.Blur blurStyle = null;
        switch (getConfig().getMaskBlurStyle()) {
            case MaskBlurStyle.MASK_BLUR_STYLE_SOLID:
                blurStyle = BlurMaskFilter.Blur.SOLID;
                break;
            case MaskBlurStyle.MASK_BLUR_STYLE_NORMAL:
                blurStyle = BlurMaskFilter.Blur.NORMAL;
                break;
        }
        mPaint.setMaskFilter(new BlurMaskFilter(15, blurStyle));
        screenW = com.cwlguideview.MeasureHelpUtil.getScreenSize(getContext())[0];
        screenH = com.cwlguideview.MeasureHelpUtil.getScreenSize(getContext())[1];
        maskBitmap = com.cwlguideview.MeasureHelpUtil.createBitmapSafely(screenW, screenH, Bitmap.Config.ARGB_8888, 2);
        if (maskBitmap == null) {
            throw new RuntimeException("out of memory when maskBitmap create ");
        }
        mCanvas = new Canvas(maskBitmap);
        mCanvas.drawColor(getConfig().getMaskColor());

    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (currentGuide == null) {
            canvas.drawBitmap(maskBitmap, 0, 0, null);
            return;
        } else if (lists.size() == 0) {
            if (onUserGuideListener != null) {
                onUserGuideListener.onLastGuideView(UserGuideView.this);
            }
        }
        int layerID = 0;

        //使用离屏绘制
        if (getConfig().getHighLightStyle() == HighLightStyle.HighLightSTYLE_ORIGINAL) {
            offsetPadding = 0;
            borderWidth = 0;
        } else {
            offsetPadding = getConfig().getOffsetPadding();
            borderWidth = getConfig().getBorderWidth();
        }
        layerID = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
        canvas.drawBitmap(maskBitmap, 0, 0, null);
        drawGuideView(currentGuide, canvas, layerID);
        if (alwaysShowViews != null) {
            for (Map.Entry<Bitmap, Rect> entry : alwaysShowViews.entrySet()) {
                Bitmap bitmap =entry.getKey();
                if (bitmap!=null){
                    canvas.drawBitmap(entry.getKey(), entry.getValue().left, entry.getValue().top, null);
                }else {
                    Log.d("TAG", "onDraw: alwaysShow bitmap is null");
                }
            }
        }

    }


    private void drawGuideView(List<HighLightView> highLightViews, Canvas canvas, int layerID) {
        if (highLightViews == null || highLightViews.size() < 1) {
            return;
        }
        for (HighLightView highLightView : highLightViews) {
            drawHighLightView(highLightView, layerID, canvas);
            drawTipView(highLightView, highLightView.getTipBitmap(), canvas);
        }
    }

    private void drawHighLightView(HighLightView highLightView, int layerID, Canvas canvas) {
        if (highLightView == null) {
            return;
        }
        float left = 0;
        float top = 0;
        float right = 0;
        float bottom = 0;
        float vWidth = highLightView.getLightView().getWidth();
        float vHeight = highLightView.getLightView().getHeight();
        Rect targetRect = new Rect();
        highLightView.getLightView().getGlobalVisibleRect(targetRect);
        if (config.getStatusBarHeight() == -1) {
            statusBarHeight = com.cwlguideview.MeasureHelpUtil.getStatuBarHeight(getContext());
        } else {
            statusBarHeight = config.getStatusBarHeight();
        }
        targetRect.offset(0, -statusBarHeight);
        left = targetRect.left - offsetPadding;
        top = targetRect.top - offsetPadding;
        ;
        right = targetRect.right + offsetPadding;
        ;
        bottom = targetRect.bottom + offsetPadding;
        ;

        if (left == 0) {
            left += borderWidth;
        } else if (top == 0) {
            top += borderWidth;
        } else if (right == screenW) {
            right -= borderWidth;
        } else if (bottom == screenH) {
            bottom -= borderWidth;
        }
        // 绘制高亮框
        switch (getConfig().getHighLightStyle()) {
            case HighLightStyle.HighLightSTYLE_RECT:
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                RectF rect = new RectF(left, top, right, bottom);
                canvas.drawRoundRect(rect, 20, 20, mPaint);
                mPaint.setXfermode(null);
                break;
            case HighLightStyle.HighLightSTYLE_CIRCLE:
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                float radius = vWidth < vHeight ? vWidth / 2 + 2 * offsetPadding : vHeight / 2 + 2 * offsetPadding;
                if (radius < 50) {
                    radius = 100;
                }
                canvas.drawCircle(left + offsetPadding + vWidth / 2, top + offsetPadding + vHeight / 2, radius, mPaint);
                mPaint.setXfermode(null);
                break;
            case HighLightStyle.HighLightSTYLE_OVAL:
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                RectF rectf = new RectF(left, top, right, bottom);
                canvas.drawOval(rectf, mPaint);
                mPaint.setXfermode(null);
                break;
            case HighLightStyle.HighLightSTYLE_ORIGINAL:
                mORIGINALPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                Bitmap b = com.cwlguideview.MeasureHelpUtil.drawViewToBitmap(highLightView.getLightView());
                canvas.drawBitmap(b, left, top, mORIGINALPaint);
                mORIGINALPaint.setXfermode(null);
                break;
        }
        canvas.restoreToCount(layerID);
    }

    private void drawTipView(HighLightView highLightView, Bitmap tipBitmap, Canvas canvas) {
        if (tipBitmap == null) {
            return;
        }
        float left = 0;
        float top = 0;
        float right = 0;
        float bottom = 0;
        float vWidth = highLightView.getLightView().getWidth();
        float vHeight = highLightView.getLightView().getHeight();
        Rect targetRect = new Rect();
        highLightView.getLightView().getGlobalVisibleRect(targetRect);
        targetRect.offset(0, -statusBarHeight);
        left = targetRect.left - offsetPadding;
        top = targetRect.top - offsetPadding;
        right = targetRect.right + offsetPadding;
        bottom = targetRect.bottom + offsetPadding;

        int tipViewMoveX = highLightView.getMoveX();
        int tipViewMoveY = highLightView.getMoveY();
        Direction direction = highLightView.getDirection();

        float tipTop = getTipTop(bottom, top, vHeight, tipBitmap, direction);
        float tipLeft = getTipLeft(left, right, vWidth, tipBitmap, direction);
        canvas.drawBitmap(tipBitmap, tipLeft + tipViewMoveX, tipTop + tipViewMoveY, null);
        tipViewHitRect = new Rect((int) (tipLeft + tipViewMoveX), (int) tipTop + tipViewMoveY, (int) tipLeft + tipViewMoveX + tipBitmap.getWidth(), (int) tipTop + tipViewMoveY + tipBitmap.getHeight());
    }

    private float getTipTop(float targetBottom, float targetTop, float targetHeight, Bitmap tipBitmap, Direction direction) {
        float top = 0;
        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            top = (targetBottom - targetTop) / 2 + targetTop - tipBitmap.getHeight() / 2;
        } else if (direction == Direction.TOP) {
            top = targetBottom;
        } else if (direction == Direction.BOTTOM) {
            top = targetTop - tipBitmap.getHeight();
        } else {
            top = (targetBottom - targetTop) / 2 + targetTop - tipBitmap.getHeight() / 2;
        }
        return top;
    }

    private float getTipLeft(float targetLeft, float targetRight, float targetWidth, Bitmap tipBitmap, Direction direction) {
        float left = 0;
        if (direction == Direction.TOP || direction == Direction.BOTTOM) {
            left = targetLeft - (tipBitmap.getWidth() - targetWidth) / 2;
        } else if (direction == Direction.LEFT) {
            left = targetRight;
        } else if (direction == Direction.RIGHT) {
            left = targetLeft - tipBitmap.getWidth();
        } else if (direction == Direction.DEFAULT) {
            left = targetLeft - (tipBitmap.getWidth() - targetWidth) / 2;
        }
        return left;
    }


    /**
     * set the TouchOutside Dismiss listener
     *
     * @param cancel
     */
    public void setTouchOutsideDismiss(boolean cancel) {
        this.touchOutsideCancel = cancel;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP://
                if (touchOutsideCancel) {
                    nextGuide();
                    return true;
                } else {
                    int touchX = (int) event.getX();
                    int touchY = (int) event.getY();
                    if (tipViewHitRect != null && tipViewHitRect.contains(touchX, touchY)) {
                        nextGuide();
                        return true;
                    }
                }
        }
        return true;
    }

    public void setOnUserGuideListener(UserGuideListener listener) {
        this.onUserGuideListener = listener;
    }

    public interface UserGuideListener {
        void onDismiss(UserGuideView userGuideView);

        void onLastGuideView(UserGuideView userGuideView);
    }


    /**
     * 开始引导 所有的设置需要在开始之前设置 否则无效
     */
    public void startGuide() {
        Log.i("TAG", "startGuide: " + lists.size());
        nextGuide();
    }


    /**
     * 获得字符行数
     *
     * @param tipStr
     * @return
     */
    private int getNewLineNum(String tipStr) {
        int total = 1;
        while (tipStr.indexOf("\n") != -1) {
            total++;
            tipStr = tipStr.substring(tipStr.indexOf("\n") + 1, tipStr.length());
        }
        return total;
    }

    /**
     * 常亮视图，view没有显示在UI上了
     *
     * @param view
     * @param rect
     */
    public UserGuideView putAlwaysShowView(View view, Rect rect) {
        if (alwaysShowViews == null) {
            alwaysShowViews = new LinkedHashMap<>();
        }
        alwaysShowViews.put(com.cwlguideview.MeasureHelpUtil.drawViewToBitmap(view, rect.width(), rect.height()), rect);
        return this;
    }

    public UserGuideView putAlwaysShowView(Bitmap bitmap, Rect rect) {
        if (alwaysShowViews == null) {
            alwaysShowViews = new LinkedHashMap<>();
        }
        alwaysShowViews.put(bitmap,  rect);
        return this;
    }

    /**
     * 常亮试图，view已经显示在UI上了
     * @param view
     * @return
     */
    public UserGuideView putAlwaysShowView(View view) {
        if (alwaysShowViews == null) {
            alwaysShowViews = new LinkedHashMap<>();
        }
        alwaysShowViews.put(com.cwlguideview.MeasureHelpUtil.drawViewToBitmap(view),new Rect(view.getLeft(),view.getTop(),view.getRight(),view.getBottom()));
        return this;
    }
    /**
     * 关闭引导
     */
    public void cancel() {
        lists = null;
        currentGuide = null;
        setVisibility(GONE);
    }

    /**
     * 下一个引导
     */
    public void nextGuide() {
        if (lists == null || lists.size() < 1) {
            if (onUserGuideListener != null) {
                onUserGuideListener.onDismiss(UserGuideView.this);
            }
            return;
        } else {
            currentGuide = lists.get(0);
            lists.remove(0);
            invalidate();
        }
    }

    //展示蒙版层
    public void setDefaultCoverLayer() {
        invalidate();
        setVisibility(VISIBLE);
    }


    public UserGuideView setConfig(Config config) {
        this.config = config;
        return this;
    }

    public UserGuideView putGuideView(HighLightView highLightView) {
        List<HighLightView> lightViews = new ArrayList<>(1);
        lightViews.add(highLightView);
        return this.putGuideTogetherViews(lightViews);
    }

    public UserGuideView putGuideTogetherViews(List<HighLightView> highLightViews) {
        for (HighLightView highLightView : highLightViews) {
            if (!TextUtils.isEmpty(highLightView.getTipText())) {
                TextView textView = new TextView(getContext());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, com.cwlguideview.MeasureHelpUtil.sp2px(getContext(), getConfig().getTipTextSize()));
                textView.setText(highLightView.getTipText());
                textView.setTextColor(getConfig().getTipTextColor());
                TextPaint textPaint = textView.getPaint();
                Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
                float textHeight = fontMetrics.bottom - fontMetrics.top;
                float textWidth = textPaint.measureText(textView.getText().toString());
                int LineNum = getNewLineNum(highLightView.getTipText());
                Bitmap tipBitmap = com.cwlguideview.MeasureHelpUtil.drawViewToBitmap(textView, (int) textWidth, (int) textHeight * LineNum);
                highLightView.setTipBitmap(tipBitmap);
            } else if (highLightView.getTipResId() != -1) {
                highLightView.setTipBitmap(BitmapFactory.decodeResource(getResources(), highLightView.getTipResId()));
            }else if (highLightView.getTipView() != null){
                highLightView.setTipBitmap(com.cwlguideview.MeasureHelpUtil.drawViewToBitmap(highLightView.getTipView(),highLightView.getTipRect().width(),highLightView.getTipRect().height()));
            }
        }
        lists.add(highLightViews);
        return this;
    }

    public Config getConfig() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }

}
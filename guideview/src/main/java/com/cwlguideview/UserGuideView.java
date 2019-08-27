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
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * create by chengwl
 * this is a common userGuideView
 * support four kinds of highLight style and one guide can show multiple highLightView
 * you can set different tip view  String or Drawable and you can also set offset that you want
 */
public class UserGuideView extends View {
    public static final int HighLightSTYLE_RECT = 0;
    public static final int HighLightSTYLE_CIRCLE = 1;
    public static final int HighLightSTYLE_OVAL = 2;
    public static final int HighLightSTYLE_ORIGINAL=3; //当设置改高亮模式的时候 边界宽度 偏移量 都将无效
    public static final int MASKBLURSTYLE_SOLID = 0;
    public static final int MASKBLURSTYLE_NORMAL = 1;
    private Bitmap maskBitmap;// 前景
    private Canvas mCanvas;// 绘制蒙版层的画布
    private Paint mPaint;// 绘制蒙版层画笔
    private Paint mORIGINALPaint;
    private int screenW, screenH;// 屏幕宽高
    private boolean touchOutsideCancel = false; // 外部关闭当前引导
    private float borderWidth = 10;// 边界余量
    private float offsetMargin = 10;// 光圈放大偏移值
    private int margin = 10;
    private int highLightStyle = HighLightSTYLE_RECT;
    public int maskblurstyle = MASKBLURSTYLE_SOLID;



    private float radius;
    private int maskColor = 0x99000000;// 蒙版层颜色
    private UserGuideListener onUserGuideListener;
    private int statusBarHeight = 0;// 状态栏高度
    private ArrayList<LinkedHashMap<View ,Bitmap>> highLightList=new ArrayList<>(); //所有的guide的集合
    private LinkedHashMap<View,Bitmap> currentGuide;//当前的引导视图

    private Rect tipViewHitRect;

    private int tipViewMoveX;
    private int tipViewMoveY;
    private Direction direction;
    private LinkedHashMap<View,Integer> tipViewMoveXMap = new LinkedHashMap<>();
    private LinkedHashMap<View,Integer> tipViewMoveYMap = new LinkedHashMap<>();
    private LinkedHashMap<View,Direction> tipViewDirectionMap = new LinkedHashMap<>();

    private LinkedHashMap<Bitmap,Rect> alwaysShowViews;

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
            highLightStyle = array.getInt(R.styleable.UserGuideView_HighlightViewStyle, HighLightSTYLE_RECT);
            maskblurstyle = array.getInt(R.styleable.UserGuideView_MaskBlurStyle, MASKBLURSTYLE_SOLID);
            maskColor = array.getColor(R.styleable.UserGuideView_maskColor, maskColor);
            offsetMargin=array.getDimension(R.styleable.UserGuideView_offsetMargin,offsetMargin);
            array.recycle();
        }
        // 计算参数
        cal(context);

        // 初始化对象
        init(context);
    }

    /**
     * 计算参数
     *
     * @param context
     */
    private void cal(Context context) {
        int[] screenSize = MeasureHelpUtil.getScreenSize(context);
        screenW = screenSize[0];
        screenH = screenSize[1];
    }

    /**
     * 初始化对象
     */
    private void init(Context context) {
        //禁用硬件加速 否则在一些机型上 会显示不出来
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        setLayerType(LAYER_TYPE_SOFTWARE,null);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

        mPaint.setARGB(0, 255, 0, 0);
        mORIGINALPaint=new Paint();
        BlurMaskFilter.Blur blurStyle = null;
        switch (maskblurstyle) {
            case MASKBLURSTYLE_SOLID:
                blurStyle = BlurMaskFilter.Blur.SOLID;
                break;
            case MASKBLURSTYLE_NORMAL:
                blurStyle = BlurMaskFilter.Blur.NORMAL;
                break;
        }
        mPaint.setMaskFilter(new BlurMaskFilter(15, blurStyle));

        maskBitmap = MeasureHelpUtil.createBitmapSafely(screenW, screenH, Bitmap.Config.ARGB_8888, 2);
        if (maskBitmap == null) {
            throw new RuntimeException("out of memory when maskBitmap create ");
        }
        mCanvas = new Canvas(maskBitmap);
        mCanvas.drawColor(maskColor);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (currentGuide == null) {
            canvas.drawBitmap(maskBitmap, 0, 0, null);
            return;
        }else if (highLightList.size()==0){
            if (onUserGuideListener!=null){
                onUserGuideListener.onLastGuideView(UserGuideView.this);
            }
        }
        int layerID=0;

        //使用离屏绘制
        if (highLightStyle==HighLightSTYLE_ORIGINAL){
            offsetMargin=0;
            borderWidth=0;
        }
        layerID = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
        canvas.drawBitmap(maskBitmap, 0, 0, null);

//        int left = targetView.getLeft();
//        int top = targetView.getTop();
        drawGuideView(currentGuide,canvas,layerID);

        if (alwaysShowViews!=null){
            for (Map.Entry<Bitmap,Rect> entry:alwaysShowViews.entrySet()){
                canvas.drawBitmap(entry.getKey(),entry.getValue().left,entry.getValue().top,null);
            }
        }

    }



    private void drawGuideView(LinkedHashMap<View,Bitmap> linkedHashMap,Canvas canvas,int layerID) {
        if (linkedHashMap==null||linkedHashMap.size()<1){
            return;
        }
        for (Map.Entry<View,Bitmap> entry:linkedHashMap.entrySet()){
            drawHighLightView(entry.getKey(),layerID,canvas);
            drawTipView(entry.getKey(),entry.getValue(),canvas);
        }
    }
    private void drawHighLightView(View highLightView,int layerID,Canvas canvas){
        if (highLightView==null){
            return;
        }
        float left = 0;
        float top = 0;
        float right = 0;
        float bottom = 0;
        float vWidth = highLightView.getWidth();
        float vHeight = highLightView.getHeight();
        Rect tagetRect = new Rect();
        highLightView.getGlobalVisibleRect(tagetRect);
        tagetRect.offset(0, -statusBarHeight);
        left = tagetRect.left - offsetMargin;
        top = tagetRect.top - offsetMargin;
        right = tagetRect.right + offsetMargin;
        bottom = tagetRect.bottom + offsetMargin;

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
        switch (highLightStyle) {
            case HighLightSTYLE_RECT:
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                RectF rect = new RectF(left, top, right, bottom);
                canvas.drawRoundRect(rect, 20, 20, mPaint);
                mPaint.setXfermode(null);
                break;
            case HighLightSTYLE_CIRCLE:
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                radius = vWidth < vHeight ? vWidth / 2 + 2 * offsetMargin : vHeight / 2 + 2 * offsetMargin;
                if (radius < 50) {
                    radius = 100;
                }
                canvas.drawCircle(left + offsetMargin + vWidth / 2, top + offsetMargin + vHeight / 2, radius, mPaint);
                mPaint.setXfermode(null);
                break;
            case HighLightSTYLE_OVAL:
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                RectF rectf = new RectF(left, top, right, bottom);
                canvas.drawOval(rectf, mPaint);
                mPaint.setXfermode(null);
                break;
            case HighLightSTYLE_ORIGINAL:
//                LoggerUtil.logInfo("left "+left+" top "+top);
                mORIGINALPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
                Bitmap b= MeasureHelpUtil.drawViewToBitmap(highLightView);
                canvas.drawBitmap(b,left,top,mORIGINALPaint);
                mORIGINALPaint.setXfermode(null);
                break;
        }
        canvas.restoreToCount(layerID);
    }

    private void drawTipView(View highLightView,Bitmap tipBitmap, Canvas canvas) {
        if (tipBitmap==null){
            return;
        }
        float left = 0;
        float top = 0;
        float right = 0;
        float bottom = 0;
        float vWidth = highLightView.getWidth();
        float vHeight = highLightView.getHeight();
        Rect targetRect = new Rect();
        highLightView.getGlobalVisibleRect(targetRect);
        targetRect.offset(0, -statusBarHeight);
        left = targetRect.left - offsetMargin;
        top = targetRect.top - offsetMargin;
        right = targetRect.right + offsetMargin;
        bottom = targetRect.bottom + offsetMargin;

        tipViewMoveX = getTipViewMoveX(highLightView);
        tipViewMoveY = getTipViewMoveY(highLightView);
        direction=getTipViewDirection(highLightView);

        float tipTop=getTipTop(bottom,top,vHeight,tipBitmap,direction);
        float tipLeft=getTipLeft(left,right,vWidth,tipBitmap,direction);
        canvas.drawBitmap(tipBitmap, tipLeft+tipViewMoveX, tipTop + tipViewMoveY, null);
        tipViewHitRect = new Rect((int)(tipLeft +tipViewMoveX), (int)tipTop + tipViewMoveY, (int)tipLeft +tipViewMoveX+ tipBitmap.getWidth(), (int)tipTop + tipViewMoveY+ tipBitmap.getHeight());
//        int tipTop = getTipTop(bottom, top, vHeight, tipBitmap);
//        if (bottom < screenH / 2 || (screenH / 2 - top > bottom - screenH / 2)) {// top
//            if (right < screenW / 2 || (screenW / 2 - left > right - screenW / 2)) {//left
//                canvas.drawBitmap(tipBitmap, right + tipViewMoveX, tipTop + tipViewMoveY, null);
//                tipViewHitRect = new Rect(right + tipViewMoveX, tipTop + tipViewMoveY, left + tipBitmap.getWidth(), tipTop + tipBitmap.getHeight());
//            }
//         else if (screenW / 2 - 10 <= right - offsetMargin - vWidth / 2 && right - offsetMargin - vWidth / 2 <= screenW / 2 + 10) {// center
//            canvas.drawBitmap(tipBitmap, left + tipViewMoveX, tipTop + tipViewMoveY, null);
//            tipViewHitRect = new Rect(left + tipViewMoveX, tipTop + tipViewMoveY, left + tipBitmap.getWidth(), tipTop + tipBitmap.getHeight());
//
//        } else { //right
//            canvas.drawBitmap(tipBitmap, left -tipBitmap.getWidth()+tipViewMoveX, tipTop + tipViewMoveY, null);
//            tipViewHitRect = new Rect(left -tipViewMoveX, tipTop + tipViewMoveY, left + tipBitmap.getWidth(), tipTop + tipBitmap.getHeight());
//        }
//        } else {// bottom
//            if (right < screenW / 2 || (screenW / 2 - left > right - screenW / 2)) {// 左
//                canvas.drawBitmap(tipBitmap, right + tipViewMoveX, tipTop + tipViewMoveY, null);
//                tipViewHitRect = new Rect(right + tipViewMoveX, tipTop + tipViewMoveY, left + tipBitmap.getWidth(), tipTop + tipBitmap.getHeight());
//            } else if (screenW / 2 - 10 <= right - offsetMargin - vWidth / 2 && right - offsetMargin - vWidth / 2 <= screenW / 2 + 10) {// 如果基本在中间(screenW/2-10<=target的中线<=screenW/2+10)
//                canvas.drawBitmap(tipBitmap, left +tipViewMoveX, tipTop+tipViewMoveY, null);
//                tipViewHitRect = new Rect(left +tipViewMoveX,tipTop+tipViewMoveY,left +tipBitmap.getWidth(),tipTop+tipBitmap.getHeight());
//            } else {// 右
//
//            }
//        }
    }

    private Direction getTipViewDirection(View highLightView) {
        if (tipViewDirectionMap.containsKey(highLightView)){
            return tipViewDirectionMap.get(highLightView);
        }else {
            return Direction.DEFAULT;
        }
    }


    private float  getTipTop(float targetBottom,float targetTop, float targetHeight,Bitmap tipBitmap,Direction direction) {
        float top = 0;
        if (direction== Direction.LEFT||direction== Direction.RIGHT){
            top=(targetBottom-targetTop)/2+targetTop-tipBitmap.getHeight()/2;
        }else if (direction== Direction.TOP){
            top=targetBottom;
        }else if (direction== Direction.BOTTOM){
            top=targetTop-tipBitmap.getHeight();
        }else {
            top=(targetBottom-targetTop)/2+targetTop-tipBitmap.getHeight()/2;
        }
        return top;
    }
    private float  getTipLeft(float targetLeft,float targetRight, float targetWidth,Bitmap tipBitmap,Direction direction) {
        float left = 0;
        if (direction== Direction.TOP||direction== Direction.BOTTOM){
            left=targetLeft-(tipBitmap.getWidth()-targetWidth)/2;
        }else if (direction== Direction.LEFT){
            left=targetRight;
        }else if (direction== Direction.RIGHT){
            left=targetLeft-tipBitmap.getWidth();
        }else if (direction== Direction.DEFAULT){
            left=targetLeft-(tipBitmap.getWidth()-targetWidth)/2;
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

    /**
     * 设置额外的边框宽度
     *
     * @param borderWidth
     */
    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }





    private Bitmap getBitmapFromResId(int resId){
        return BitmapFactory.decodeResource(getResources(), resId);
    }



    /**
     * set cover mask color
     *
     * @param maskColor
     */
    public void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
    }

    /**
     * @param statusBarHeight
     */
    public void setStatusBarHeight(int statusBarHeight) {
        this.statusBarHeight = statusBarHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP://
                if (touchOutsideCancel) {
                   nextGuide();
                    return true;
                }else{
                   int touchX = (int) event.getX();
                   int touchY = (int) event.getY();
                   if(tipViewHitRect!=null&&tipViewHitRect.contains(touchX,touchY)){
                      nextGuide();
                       return true;
                   }
                }
        }
        return true;
    }

    /**
     * 箭头和tipview之间的margin
     * @return
     */
    public int getMargin() {
        return margin;
    }
    /**
     * 箭头和tipview之间的margin
     * @return
     */
    public void setMargin(int margin) {
        this.margin = margin;
    }

    public float getOffestMargin() {
        return offsetMargin;
    }



    /**
     * 光圈放大偏移值
     *
     * @param offsetMargin
     */
    public void setOffestMargin(float offsetMargin) {
        this.offsetMargin = offsetMargin;
    }

    public void setOnUserGuideListener(UserGuideListener listener) {
        this.onUserGuideListener = listener;
    }

    public interface UserGuideListener{
         void onDismiss(UserGuideView userGuideView);
        void onLastGuideView(UserGuideView userGuideView);
    }


    /**
     * 设置tipview的水平位移 来微调位置
     * @param highlightView 与tipview对应的高亮view
     * @param tipViewMoveX >0 向右偏移 <0向左偏移
     */
    public void setTipViewMoveX(View highlightView,int tipViewMoveX){
        tipViewMoveXMap.put(highlightView,tipViewMoveX);
    }
    /**
     * 设置tipview的垂直位移
     * @param highlightView 与tipview对应的高亮view
     * @param tipViewMoveY >0 向右偏移 <0向左偏移
     */
    public void setTipViewMoveY(View highlightView,int tipViewMoveY){
        tipViewMoveYMap.put(highlightView,tipViewMoveY);
    }
    public void setTipDirection(View highlightView,Direction dirction){
        tipViewDirectionMap.put(highlightView,dirction);
    }
    public Direction getTipDirection(View highlightView,Direction dirction){
        Direction direction = tipViewDirectionMap.get(highlightView);
        return dirction==null? Direction.DEFAULT:direction;
    }


    public int getTipViewMoveX(View highLightView) {
        Integer moveX = tipViewMoveXMap.get(highLightView);
        return moveX==null?0:moveX;
    }
    public int getTipViewMoveY(View highLightView) {
        Integer moveY = tipViewMoveYMap.get(highLightView);
        return moveY==null?0:moveY;
    }


    /**
     * 开始引导 所有的设置需要在开始之前设置 否则无效
     */
    public void startGuide(){
        nextGuide();
    }

    public void putGuideView(View highLightView,int tipViewRes,int moveX,int moveY){
        Bitmap tipBitmap=getBitmapFromResId(tipViewRes);
        addGuideView(highLightView,tipBitmap);
        setTipViewMoveX(highLightView,moveX);
        setTipViewMoveY(highLightView,moveY);
    }
    public void putGuideView(View highLightView,int tipViewRes,int moveX,int moveY,Direction direction){
        Bitmap tipBitmap=getBitmapFromResId(tipViewRes);
        addGuideView(highLightView,tipBitmap);
        setTipViewMoveX(highLightView,moveX);
        setTipViewMoveY(highLightView,moveY);
        setTipDirection(highLightView,direction);
    }

    /**
     *
     * @param highLightView
     * @param tipBitmap
     * @param moveX 横向偏移量 >0 向右 <0 向左
     * @param moveY 纵向偏移量 >0 向下 <0 向上
     */
    public void putGuideView(View highLightView,Bitmap tipBitmap,int moveX,int moveY){
        addGuideView(highLightView,tipBitmap);
        setTipViewMoveX(highLightView,moveX);
        setTipViewMoveY(highLightView,moveY);
    }

    /**
     *
     * @param highLightView 高亮视图
     * @param tipBitmap 提醒图
     */
    public void putGuideView(View highLightView,Bitmap tipBitmap){
        addGuideView(highLightView,tipBitmap);
    }

    /**
     *
     * @param highLightView 高亮视图是没有显示出来 调用此方法
     * @param width 高亮视图的宽
     * @param height 高亮视图的宽
     * @param tipBitmap 为null 则tip不显示
     */
    public void putGuideView(View highLightView,int width,int height,Bitmap tipBitmap){
        int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        highLightView.measure(widthSpec,heightSpec);
        highLightView.layout(0,0,width,height);
        addGuideView(highLightView,tipBitmap);
    }

    /**
     *
     * @param highLightView
     * @param tipStr 提醒文案
     * @param moveX 横向偏移量 >0 向右 <0 向左
     * @param moveY 纵向偏移量 >0 向下 <0 向上
     * @param direction  高亮视图的位置 如果为right 提醒文案方向则在反方向，在高亮视图的left
     */
    public void putGuideView(View highLightView,String tipStr,int moveX,int moveY,Direction direction){
        TextView textView=new TextView(getContext());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, MeasureHelpUtil.sp2px(getContext(),18));
        textView.setText(tipStr);
        textView.setTextColor(getContext().getResources().getColor(R.color.white));
        TextPaint textPaint = textView.getPaint();
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float textHeight = fontMetrics.bottom - fontMetrics.top;
        float textWidth=textPaint.measureText(textView.getText().toString());
        int LineNum=getNewLineNum(tipStr);
        Bitmap tipBitmap=  MeasureHelpUtil.drawViewToBitmap(textView,(int)textWidth,(int)textHeight*LineNum);
        addGuideView(highLightView,tipBitmap);
        setTipViewMoveX(highLightView,moveX);
        setTipViewMoveY(highLightView,moveY);
        setTipDirection(highLightView,direction);
    }

    /**
     * 获得字符行数
     * @param tipStr
     * @return
     */
    private int getNewLineNum(String tipStr) {
        int total=1;
       while (tipStr.indexOf("\n")!=-1){
           total++;
           tipStr=tipStr.substring(tipStr.indexOf("\n")+1,tipStr.length());
       }
       return total;
    }

    /**
     *
     * @param highLightView
     * @param tipViewRes  提醒视图的 resId
     */
    public void putGuideView(View highLightView,int tipViewRes){
        Bitmap tipBitmap=getBitmapFromResId(tipViewRes);
        addGuideView(highLightView,tipBitmap);
    }

    private void addGuideView(View highLightView,Bitmap tipBitmap){
        if (highLightView!=null){
            LinkedHashMap<View,Bitmap> bitmapLinkedHashMap=new LinkedHashMap<>();
            bitmapLinkedHashMap.put(highLightView,tipBitmap);
            highLightList.add(bitmapLinkedHashMap);
        }
    }

    /**
     *  set highlight views .it will display them by order will use the same tipview
     */
    public void setGuideViewWithDefaultTip(int tipRes,View... highLightView){
        if(highLightView!=null){
            for(int i=0;i<highLightView.length;i++){
                Bitmap tipBitmap=getBitmapFromResId(tipRes);
                addGuideView(highLightView[i],tipBitmap);
            }
        }
    }
    private void addGuideView(LinkedHashMap<View,Bitmap> bitmapLinkedHashMap){
        highLightList.add(bitmapLinkedHashMap);
    }
//    public void addGuideView(LinkedHashMap<View,String> bitmapLinkedHashMap){
//        highLightList.add(bitmapLinkedHashMap);
//    }

    /**
     *

     */
    public void putGuideView(LinkedHashMap<View,Bitmap> linkedHashMap){
        if(linkedHashMap!=null){
            addGuideView(linkedHashMap);
        }
    }

    /**
     *
     * @param linkedHashMap 当前整个引导页高亮视图 例如可以一下展示 两个高亮view
     * @param directionLinkedHashMap 方向
     */
    public void putGuideView(LinkedHashMap<View,String> linkedHashMap,LinkedHashMap<View,Direction> directionLinkedHashMap){
        if(linkedHashMap!=null){
            LinkedHashMap<View,Bitmap> bitmapLinkedHashMap=new LinkedHashMap<>();
            for (Map.Entry<View,String> entry:linkedHashMap.entrySet()){
                TextView textView=new TextView(getContext());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, MeasureHelpUtil.sp2px(getContext(),18));
                textView.setText(entry.getValue());
                textView.setTextColor(getContext().getResources().getColor(R.color.white));
                TextPaint textPaint = textView.getPaint();
                Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
                float textHeight = fontMetrics.bottom - fontMetrics.top;
                float textWidth=textPaint.measureText(entry.getValue());
                Bitmap tipBitmap=  MeasureHelpUtil.drawViewToBitmap(textView,(int)textWidth,(int)textHeight);
                bitmapLinkedHashMap.put(entry.getKey(),tipBitmap);
                if (directionLinkedHashMap.containsKey(entry.getKey())){
                    tipViewDirectionMap.put(entry.getKey(),directionLinkedHashMap.get(entry.getKey()));
                }
            }
            addGuideView(bitmapLinkedHashMap);
        }
    }


    /**
     * 常驻视图
     * @param view
     * @param rect
     */
    public void putAlwaysShowView(View view,Rect rect){
        if (alwaysShowViews==null){
            alwaysShowViews=new LinkedHashMap<>();
        }
        alwaysShowViews.put(MeasureHelpUtil.drawViewToBitmap(view,rect.width(),rect.height()),rect);
    }

    /**
     * 关闭引导
     */
    public void cancel(){
        tipViewDirectionMap=null;
        currentGuide=null;
        tipViewMoveXMap=null;
        tipViewMoveYMap=null;
        setVisibility(GONE);
    }

    /**
     * 下一个引导
     */
    public void nextGuide(){
        if (highLightList==null||highLightList.size()<1){
            if (onUserGuideListener!=null){
                onUserGuideListener.onDismiss(UserGuideView.this);
            }
            return;
        }else {
           currentGuide=highLightList.get(0);
           highLightList.remove(0);
           invalidate();
        }
    }
    //展示蒙版层
    public void setDefaultCoverLayer(){
        invalidate();
        setVisibility(VISIBLE);
    }
   //高亮view的摆放方向
   public enum Direction{
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        DEFAULT;
    }
}
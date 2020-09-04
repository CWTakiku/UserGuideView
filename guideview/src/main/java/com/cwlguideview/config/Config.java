package com.cwlguideview.config;

import android.graphics.Color;

public class Config {
    private static final int DEFAULT_TEXT_SIZE = 16;
    private static final int DEFAULT_TEXT_COLOR = Color.WHITE;
    private static final int DEFAULT_MASK_COLOR = 0x99000000;
    private static final int DEFAULT_OFFSET_PADDING = 10;
    private static final int DEFAULT_BORDER_WIDTH = 10;

    /**
     * 提醒文案字体大小
     */
    private int tipTextSize = DEFAULT_TEXT_SIZE;
    /**
     * 提醒文案字体颜色
     */
    private int tipTextColor = DEFAULT_TEXT_COLOR;
    /**
     * 亮圈的padding
     */
    private float offsetPadding = DEFAULT_OFFSET_PADDING;
    /**
     * 遮蒙层颜色
     */
    private int maskColor = DEFAULT_MASK_COLOR;
    /**
     * 状态栏高度
     */
    private int statusBarHeight = -1;
    /**
     * 亮圈的边宽
     */
    private int borderWidth = DEFAULT_BORDER_WIDTH;
    /**
     * 高亮类型
     */
    private int highLightStyle = HighLightStyle.HighLightSTYLE_RECT;
    /**
     *
     */
    private int maskBlurStyle = MaskBlurStyle.MASK_BLUR_STYLE_NORMAL;


    public int getTipTextSize() {
        return tipTextSize;
    }

    public Config setTipTextSize(int tipTextSize) {
        this.tipTextSize = tipTextSize;
        return this;
    }

    public int getTipTextColor() {
        return tipTextColor;
    }

    public Config setTipTextColor(int tipTextColor) {
        this.tipTextColor = tipTextColor;
        return this;
    }

    public float getOffsetPadding() {
        return offsetPadding;
    }

    public Config setOffsetPadding(float offsetPadding) {
        this.offsetPadding = offsetPadding;
        return this;
    }

    public int getStatusBarHeight() {
        return statusBarHeight;
    }

    public Config setStatusBarHeight(int statusBarHeight) {
        this.statusBarHeight = statusBarHeight;
        return this;
    }

    public int getMaskColor() {
        return maskColor;
    }

    public Config setMaskColor(int maskColor) {
        this.maskColor = maskColor;
        return this;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public Config setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        return this;
    }

    public int getHighLightStyle() {
        return highLightStyle;
    }

    public Config setHighLightStyle(int highLightStyle) {
        this.highLightStyle = highLightStyle;
        return this;
    }

    public int getMaskBlurStyle() {
        return maskBlurStyle;
    }

    public Config setMaskBlurStyle(int maskBlurStyle) {
        this.maskBlurStyle = maskBlurStyle;
        return this;
    }
}
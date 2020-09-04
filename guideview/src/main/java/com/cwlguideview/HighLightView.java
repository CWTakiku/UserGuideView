package com.cwlguideview;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;

import com.cwlguideview.config.Direction;

public class HighLightView {
    private View lightView;
    private String tipText;
    private View tipView;
    private int tipResId = -1;
    protected Bitmap tipBitmap;
    private int moveX;
    private int moveY;
    private Direction direction;
    private Rect tipRect;

    public HighLightView(View lightView, String tipText, Direction direction) {
        this(lightView, tipText, 0, 0, direction);
    }

    public HighLightView(View lightView, int tipResId, Direction direction) {
        this(lightView, tipResId, 0, 0, direction);
    }

    public HighLightView(View lightView, Bitmap tipBitmap, Direction direction) {
        this.lightView = lightView;
        this.tipBitmap = tipBitmap;
        this.direction = direction;
        this.moveX = moveX;
        this.moveY = moveY;
    }

    public HighLightView(View lightView, View tipView, Rect tipRect, int moveX,int moveY,Direction direction) {
        this.lightView = lightView;
        this.tipView = tipView;
        this.tipRect= tipRect;
        this.direction = direction;
        this.moveX = moveX;
        this.moveY = moveY;
    }

    public HighLightView(View lightView, int tipResId, int moveX, int moveY, Direction direction) {
        this.lightView = lightView;
        this.tipResId = tipResId;
        this.direction = direction;
        this.moveX = moveX;
        this.moveY = moveY;
    }

    public HighLightView(View lightView, String tipText, int moveX, int moveY, Direction direction) {
        this.lightView = lightView;
        this.tipText = tipText;
        this.direction = direction;
        this.moveX = moveX;
        this.moveY = moveY;
    }

    public Bitmap getTipBitmap() {
        return tipBitmap;
    }

    protected void setTipBitmap(Bitmap tipBitmap) {
        this.tipBitmap = tipBitmap;
    }

    public View getLightView() {
        return lightView;
    }

    public String getTipText() {
        return tipText == null ? "" : tipText;
    }

    public int getTipResId() {
        return tipResId;
    }

    public int getMoveX() {
        return moveX;
    }

    public int getMoveY() {
        return moveY;
    }

    public Direction getDirection() {
        return direction;
    }

    public View getTipView() {
        return tipView;
    }

    public Rect getTipRect() {
        return tipRect;
    }
}
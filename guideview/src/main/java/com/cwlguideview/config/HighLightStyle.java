package com.cwlguideview.config;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.cwlguideview.config.HighLightStyle.HighLightSTYLE_CIRCLE;
import static com.cwlguideview.config.HighLightStyle.HighLightSTYLE_OVAL;
import static com.cwlguideview.config.HighLightStyle.HighLightSTYLE_RECT;

@IntDef({HighLightSTYLE_RECT, HighLightSTYLE_CIRCLE, HighLightSTYLE_OVAL, HighLightStyle.HighLightSTYLE_ORIGINAL})
@Retention(value = RetentionPolicy.SOURCE)
public @interface HighLightStyle {
    /**
     * 矩形
     */
    int HighLightSTYLE_RECT = 0;
    /**
     * 圆形
     */
    int HighLightSTYLE_CIRCLE = 1;
    /**
     * 椭圆
     */
    int HighLightSTYLE_OVAL = 2;
    /**
     * 原始view直接高亮，没有光圈
     */
    int HighLightSTYLE_ORIGINAL = 3; //当设置改高亮模式的时候 边界宽度 偏移量 都将无效
}
package com.cwlguideview.config;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @des:
 * @author: chengwl
 * @date: 2020/9/4
 */
@IntDef({MaskBlurStyle.MASK_BLUR_STYLE_SOLID, MaskBlurStyle.MASK_BLUR_STYLE_NORMAL})
@Retention(value = RetentionPolicy.SOURCE)
public @interface MaskBlurStyle {
     int MASK_BLUR_STYLE_SOLID = 0;
     int MASK_BLUR_STYLE_NORMAL = 1;
}

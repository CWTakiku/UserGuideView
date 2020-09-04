package com.example.userguideview;

import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cwlguideview.HighLightView;
import com.cwlguideview.MeasureHelpUtil;
import com.cwlguideview.UserGuideView;
import com.cwlguideview.config.Config;
import com.cwlguideview.config.Direction;
import com.cwlguideview.config.HighLightStyle;
import com.cwlguideview.config.MaskBlurStyle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout rlUserGuide;
    private UserGuideView userGuideView;
    private TextView tvNext;
    private TextView btnSkip;
    private TextView btnStart;
    private RelativeLayout rlGuideHint;
    private TextView tvTop;
    private TextView tvLeft;
    private TextView tvBottom;
    private ImageView ivRight;
    private TextView tv_always;
    boolean isLast=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        rlUserGuide=findViewById(R.id.rl_userGuide);
        userGuideView=findViewById(R.id.ug_view);
        tvNext=findViewById(R.id.btn_next);
        btnSkip=findViewById(R.id.btn_skip);
        btnStart=findViewById(R.id.btn_start);
        rlGuideHint=findViewById(R.id.rl_guide_hint);
        tvLeft=findViewById(R.id.tv_left);
        tvBottom=findViewById(R.id.tv_bottom);
        ivRight=findViewById(R.id.iv_right);
        tvTop=findViewById(R.id.tv_top);
        tv_always=findViewById(R.id.tv_always);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        showGuideLayer();

    }
    private void showGuideLayer() {

        rlUserGuide.setVisibility(View.VISIBLE);
        userGuideView.setDefaultCoverLayer();
        userGuideView.setOnUserGuideListener(new UserGuideView.UserGuideListener() {
            @Override
            public void onDismiss(UserGuideView userGuideView) {

            }

            @Override
            public void onLastGuideView(UserGuideView userGuideView) {
                isLast=true;
                tvNext.setText(getString(R.string.done));
            }
        });
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userGuideView.cancel();
                rlUserGuide.setVisibility(View.GONE);
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rlGuideHint.setVisibility(View.GONE);

                HighLightView leftView = new HighLightView(tvLeft,"this is a left view",20,0, Direction.LEFT);
                HighLightView topView = new HighLightView(tvTop,R.drawable.bettery_camera_50to60,0,30, Direction.TOP);
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.right_hint_layout,null,false);
                HighLightView rightView = new HighLightView(ivRight,view,new Rect(0,0, MeasureHelpUtil.dip2px(MainActivity.this,150), MeasureHelpUtil.dip2px(MainActivity.this,40)),-50,0,Direction.RIGHT);
                HighLightView bottomView = new HighLightView(tvBottom,"this is a bottom view",-100,0,Direction.BOTTOM);
                List<HighLightView> togetherShowViews = new ArrayList<>();
                togetherShowViews.add(rightView);
                togetherShowViews.add(bottomView);


                userGuideView.setConfig(new Config().setHighLightStyle(HighLightStyle.HighLightSTYLE_ORIGINAL).setMaskBlurStyle(MaskBlurStyle.MASK_BLUR_STYLE_NORMAL))
                        .putGuideView(leftView)
                        .putGuideView(topView)
                        .putGuideTogetherViews(togetherShowViews)
                        //.putAlwaysShowView(tv_always)
                        .startGuide();

                tvNext.setVisibility(View.VISIBLE);
            }
        });

        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLast){
                    userGuideView.nextGuide();
                }else {
                    userGuideView.cancel();
                    rlUserGuide.setVisibility(View.GONE);
                }
            }
        });
    }
}

package com.example.userguideview;

import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cwlguideview.MeasureHelpUtil;
import com.cwlguideview.UserGuideView;

import java.util.LinkedHashMap;

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
    private TextView tvRight;
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
        tvRight=findViewById(R.id.tv_right);
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
                userGuideView.putGuideView(tvLeft,"this is a left view",20,0, UserGuideView.Direction.LEFT);
                userGuideView.putGuideView(tvTop,R.drawable.bettery_camera_50to60,0,30, UserGuideView.Direction.TOP);
                LinkedHashMap<View,String> hashMap=new LinkedHashMap<>(2);
                hashMap.put(tvBottom,"thi is a bottom view");
                hashMap.put(tvRight,"thi is a right view");
                LinkedHashMap<View, UserGuideView.Direction> directionLinkedHashMap=new LinkedHashMap<>(2);
                directionLinkedHashMap.put(tvBottom, UserGuideView.Direction.BOTTOM);
                directionLinkedHashMap.put(tvRight, UserGuideView.Direction.RIGHT);
                userGuideView.putGuideView(hashMap,directionLinkedHashMap);
                userGuideView.setTipViewMoveY(tvBottom,-100);
                userGuideView.setTipViewMoveX(tvRight,-50);
                userGuideView.putAlwaysShowView(tv_always,new Rect(0,0,tv_always.getWidth(),tv_always.getHeight()));
                userGuideView.startGuide();

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

package com.zhangke.socketdemo;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by ZhangKe on 2018/6/14.
 */
public class LineLayout extends LinearLayout {

    private TextView tv01;
    private TextView tv02;
    private TextView tv03;
    private TextView tv04;
    private TextView tv05;

    public LineLayout(Context context) {
        super(context);
        init();
    }

    public LineLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        tv01 = new TextView(getContext());
        tv02 = new TextView(getContext());
        tv03 = new TextView(getContext());
        tv04 = new TextView(getContext());
        tv05 = new TextView(getContext());
        initTextView(tv01);
        initTextView(tv02);
        initTextView(tv03);
        initTextView(tv04);
        initTextView(tv05);
        setOrientation(HORIZONTAL);
        addView(tv01);
        addView(tv02);
        addView(tv03);
        addView(tv04);
        addView(tv05);
    }

    private void initTextView(TextView tv) {
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        tv.setLayoutParams(layoutParams);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.WHITE);
    }

    public void setInfo(GameInfo gameInfo){

    }

}

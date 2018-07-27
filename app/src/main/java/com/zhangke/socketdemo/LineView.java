package com.zhangke.socketdemo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by ZhangKe on 2018/6/14.
 */
public class LineView extends LinearLayout {

    private TextView tv01;
    private TextView tv02;
    private TextView tv03;
    private TextView tv04;
    private TextView tv05;

    public LineView(Context context) {
        super(context);
        init();
    }

    public LineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        addView(getDividerView());
        addView(tv01);
        addView(getDividerView());
        addView(tv02);
        addView(getDividerView());
        addView(tv03);
        addView(getDividerView());
        addView(tv04);
        addView(getDividerView());
        addView(tv05);
        addView(getDividerView());
    }

    private void initTextView(TextView tv) {
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        tv.setLayoutParams(layoutParams);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(UiUtil.sp2px(getContext(), 24));
    }

    private View getDividerView() {
        View view = new View(getContext());
        LayoutParams layoutParams = new LayoutParams(UiUtil.dip2px(getContext(), 1), LayoutParams.MATCH_PARENT);
        view.setLayoutParams(layoutParams);
        view.setBackground(new ColorDrawable(getResources().getColor(R.color.divider_color)));
        return view;
    }

    public void setInfo(GameInfo gameInfo) {
        if (gameInfo == null) {
            clearText();
        } else {
            tv01.setText(String.valueOf(gameInfo.getRanking()));
            tv02.setText(gameInfo.getNumber());
            tv03.setText(String.valueOf(gameInfo.getHitCount()));
            tv04.setText(String.valueOf(gameInfo.getBeHitCount()));
            tv05.setText(String.valueOf(gameInfo.getScore()));
        }
    }

    public void clearText() {
        tv01.setText("");
        tv02.setText("");
        tv03.setText("");
        tv04.setText("");
        tv05.setText("");
    }
}

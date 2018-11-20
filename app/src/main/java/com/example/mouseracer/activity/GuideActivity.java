package com.example.mouseracer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mouseracer.R;

public class GuideActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private ViewPager viewPager;
    private int[] imgs = {R.mipmap.guideone, R.mipmap.guidetwo, R.mipmap.guidethree, R.mipmap
            .guidefour, R.mipmap.guidefive};
    private TextView tvMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_guide);
        viewPager = findViewById(R.id.viewpager);
        tvMenu = findViewById(R.id.tvMenu);
        viewPager.addOnPageChangeListener(this);
        setAdapter();
    }

    private void setAdapter() {
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 5;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {//必须实现
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View view = getViewForIndex(position);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {//必须实现，销毁
                container.removeView(container.findViewWithTag(position + "lead"));
            }


            private View getViewForIndex(int index) {
                View view = LayoutInflater.from(GuideActivity.this).inflate(R.layout
                        .lead_img_view, null);
                ImageView img = view.findViewById(R.id.img);
                //TextView tvMenu = view.findViewById(R.id.tvMenu);
                tvMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(GuideActivity.this, MainActivity.class));
                        finish();
                    }
                });
//                onPageSelected(index);

//                if (index == 3) {
//                    startBtn.setVisibility(View.VISIBLE);
//                } else {
//                    startBtn.setVisibility(View.GONE);
//                }
                img.setImageResource(imgs[index]);
                view.setTag(index + "lead");
                return view;
            }
        });
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
package com.app.siy.activity.recorder;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.app.siy.R;
import com.app.siy.adapter.TutorialAdapter;
import com.app.siy.utils.Constants;

public class TutorialActivityRecorder extends AppCompatActivity implements View.OnClickListener {

    private ViewPager viewPager;
    private int layouts[] = {R.layout.recorder_layout_slide_1, R.layout.recorder_layout_slide_2, R.layout.recorder_layout_slide_3};

    private TutorialAdapter tutorialAdapter;
    //private LinearLayout dotsLayout;
    private ImageView[] dots;
    private Button btnNext;
    private Button btnSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recorder_activity_tutorial);
        viewPager = (ViewPager) findViewById(R.id.view_pager_intro_slider_recorder);

        tutorialAdapter = new TutorialAdapter(layouts, TutorialActivityRecorder.this);
        viewPager.setAdapter(tutorialAdapter);

        btnNext = (Button) findViewById(R.id.btn_next);
        btnSkip = (Button) findViewById(R.id.btn_skip);

        btnNext.setOnClickListener(this);
        btnSkip.setOnClickListener(this);

        //Instantiate dots layout.
        //dotsLayout = (LinearLayout) findViewById(R.id.dots_layout);

        //Create dots
        //createDots(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
               // createDots(position);
                if (position == layouts.length - 1) {
                    btnNext.setText("Start");
                    btnSkip.setVisibility(View.INVISIBLE);
                } else {
                    btnNext.setText("Next");
                    btnSkip.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    /*private void createDots(int currentPosition) {
        if (dotsLayout != null) {
            dotsLayout.removeAllViews();
        }

        dots = new ImageView[layouts.length];

        for (int i = 0; i < layouts.length; i++) {
            dots[i] = new ImageView(this);
            if (i == currentPosition) {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.intro_slider_active_dots));
            } else {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.intro_slider_default_dots));
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(4, 0, 4, 0);
            dotsLayout.addView(dots[i], layoutParams);
        }
    }*/

    //Disable Back Button
    @Override
    public void onBackPressed() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                loadNextSlide();
                break;

            case R.id.btn_skip:
                startActivity(new Intent(this, CompleteProfileActivityRecorder.class));
                finish();
                break;
        }
    }

    private void loadNextSlide() {
        int nextSlide = viewPager.getCurrentItem() + 1;
        if (nextSlide < layouts.length) {
            viewPager.setCurrentItem(nextSlide);
        } else {
            startActivity(new Intent(this, CompleteProfileActivityRecorder.class));
            finish();
        }
    }
}

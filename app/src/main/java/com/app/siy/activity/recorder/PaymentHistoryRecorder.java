package com.app.siy.activity.recorder;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.adapter.PaymentHistoryAdapter;
import com.app.siy.fragment.AccountDetailsFragment;
import com.app.siy.fragment.PaymentReceivedFragment;

public class PaymentHistoryRecorder extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private ImageView backImage;
    private ViewPager viewPager;
    private PaymentHistoryAdapter paymentHistoryAdapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recorder_activity_payment_history);

        toolbar = (Toolbar) findViewById(R.id.include_tool_bar_payment_history);
        TextView toolbarText = toolbar.findViewById(R.id.tool_bar_text);
        toolbarText.setText("Payment History");

        backImage = (ImageView) toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout_payment_history);

        viewPager = (ViewPager) findViewById(R.id.view_pager_payment_history);
        paymentHistoryAdapter = new PaymentHistoryAdapter(getSupportFragmentManager());

        //Add tabs
        paymentHistoryAdapter.addTab(new PaymentReceivedFragment(), "Payment Received");
        paymentHistoryAdapter.addTab(new AccountDetailsFragment(), "Account Details");

        viewPager.setAdapter(paymentHistoryAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;
        }


    }
}

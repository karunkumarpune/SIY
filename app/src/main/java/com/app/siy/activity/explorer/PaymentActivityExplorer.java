package com.app.siy.activity.explorer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.activity.EditPaymentSettingActivity;

public class PaymentActivityExplorer extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private ImageView backImage;
    private RelativeLayout debitCardLayot;
    private RelativeLayout creditCardLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explorer_activity_payment);

        debitCardLayot = (RelativeLayout) findViewById(R.id.payment_layout_debit_card);
        creditCardLayout = (RelativeLayout) findViewById(R.id.payment_layout_credit_card);

        toolbar = (Toolbar) findViewById(R.id.include_toolbar_payment_history_explorer);
        TextView textView = toolbar.findViewById(R.id.tool_bar_text);
        textView.setText("Payments");

        backImage = toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);

        debitCardLayot.setOnClickListener(this);
        creditCardLayout.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;

            case R.id.payment_layout_debit_card:
                startActivity(new Intent(this, EditPaymentSettingActivity.class));

                break;

            case R.id.payment_layout_credit_card:
                startActivity(new Intent(this, EditPaymentSettingActivity.class));
                break;

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}

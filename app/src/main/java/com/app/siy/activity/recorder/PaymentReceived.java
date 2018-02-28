package com.app.siy.activity.recorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.model.PaymentHistoryData;

public class PaymentReceived extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private ImageView backImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recorder_activity_payment_received);

        toolbar = (Toolbar) findViewById(R.id.include_toolbar_payment_received);
        TextView toolbarHeading = toolbar.findViewById(R.id.tool_bar_text);
        toolbarHeading.setText("Payment Received");

        backImage = (ImageView) toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);

        PaymentHistoryData paymentHistoryData = (PaymentHistoryData) getIntent().getExtras().getSerializable("PAYMENT_DETAILS");

        TextView amt = (TextView) findViewById(R.id.payment_received_amount);
        TextView name = (TextView) findViewById(R.id.payment_received_name);
        TextView paymentDate = (TextView) findViewById(R.id.payment_received_date_);

        amt.setText(paymentHistoryData.getReceivingAmount());
        name.setText(paymentHistoryData.getReceiverName());
        paymentDate.setText(paymentHistoryData.getReceivingDate());

    }

    /*private void setInfoToAlertDialog(String amount, String from, String date){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.recorder_layout_payment_received_dialog, null);
        builder.setView(view);


        TextView amt = (TextView) view.findViewById(R.id.payment_received_amount);
        TextView name = (TextView) view.findViewById(R.id.payment_received_name);
        TextView paymentDate = (TextView) view.findViewById(R.id.payment_received_date_);

        amt.setText(amount);
        name.setText(from);
        paymentDate.setText(date);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }*/


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;
        }
    }
}

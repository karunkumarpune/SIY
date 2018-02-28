package com.app.siy.activity;

import android.content.DialogInterface;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.MonthAndYearPicker;

public class EditPaymentSettingActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private ImageView btnBack;
    private EditText etCardHolderName;
    private EditText etCardNumber;
    private EditText etCvv;
    private TextView tvExpiryDate;
    private Button btnSaveCardDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explorer_activity_edit_payment_setting);
        etCardHolderName = (EditText) findViewById(R.id.et_card_holder_name);
        etCardNumber = (EditText) findViewById(R.id.et_card_number);
        etCvv = (EditText) findViewById(R.id.et_cvv);
        tvExpiryDate = (TextView) findViewById(R.id.tv_expiry_date);
        btnSaveCardDetails = (Button) findViewById(R.id.btn_save_card_details);

        toolbar = (Toolbar) findViewById(R.id.tool_bar_edit_payment_setting);
        TextView textView = toolbar.findViewById(R.id.tool_bar_text);
        textView.setText("Add New Card");
        btnBack = (ImageView) findViewById(R.id.back_image_tool_bar);
        btnBack.setOnClickListener(this);
        btnSaveCardDetails.setOnClickListener(this);
        tvExpiryDate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;

            case R.id.tv_expiry_date:
                displayCalendarForExpiryDate();
                break;

            case R.id.btn_save_card_details:
                saveCardDetails();
                break;
        }
    }

    private void displayCalendarForExpiryDate() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            final MonthAndYearPicker monthAndYearPicker = new MonthAndYearPicker(this);
            monthAndYearPicker.build(month, year, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int selectedMonth = monthAndYearPicker.getSelectedMonth();
                    int selectedYear = monthAndYearPicker.getSelectedYear();
                    selectedMonth++;
                    tvExpiryDate.setText(selectedMonth + "/" + selectedYear);
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            monthAndYearPicker.show();
        }

    }

    private void saveCardDetails() {
        String cardHolderName = etCardHolderName.getText().toString().trim();
        String cardNumber = etCardNumber.getText().toString().trim();

        String cvv = etCvv.getText().toString().trim();
        String expiryDate = tvExpiryDate.getText().toString().trim();

        if (TextUtils.isEmpty(cardHolderName)) {
            AppUtils.showToastBlack(this, "Please enter card holder name");
        } else if (TextUtils.isEmpty(cardNumber)) {
            AppUtils.showToastBlack(this, "Please enter card number");
        } else if (TextUtils.isEmpty(cvv)) {
            AppUtils.showToastBlack(this, "Please enter CVV");
        } else if (TextUtils.isEmpty(expiryDate)) {
            AppUtils.showToastBlack(this, "Please enter expiry date");
        } else if (cardNumber.length() < 16) {
            AppUtils.showToast(this, "Invalid Card Number");
        } else if (cvv.length() != 3) {
            AppUtils.showToastBlack(this, "CVV must be 3 digits long");
        }

        if (!TextUtils.isEmpty(cardHolderName) && !TextUtils.isEmpty(cardNumber) && !TextUtils.isEmpty(cvv) && !TextUtils.isEmpty(expiryDate) && (cardNumber.length() == 16) && (cvv.length() == 3)) {
            AppUtils.showToast(this, "Card Details Saved");
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
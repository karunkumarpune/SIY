package com.app.siy.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.app.siy.R;
import com.app.siy.activity.EditPaymentSettingActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountDetailsFragment extends Fragment implements View.OnClickListener{


    RelativeLayout creditCardLayout, debitCardLayout;


    public AccountDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recorder_fragment_account_details, container, false);
        creditCardLayout = (RelativeLayout) view.findViewById(R.id.payment_layout_credit_card);
        debitCardLayout = (RelativeLayout) view.findViewById(R.id.payment_layout_debit_card);
        creditCardLayout.setOnClickListener(this);
        debitCardLayout.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.payment_layout_credit_card:
                //Credit Card
                Intent intent = new Intent(getContext(), EditPaymentSettingActivity.class);
                startActivity(intent);
                //overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case R.id.payment_layout_debit_card:
                //Debit Card
                //Credit Card
                Intent intent2 = new Intent(getContext(), EditPaymentSettingActivity.class);
                startActivity(intent2);
                //overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
        }
    }
}

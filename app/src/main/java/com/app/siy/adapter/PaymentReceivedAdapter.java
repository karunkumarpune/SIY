package com.app.siy.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.siy.model.PaymentHistoryData;

import com.app.siy.R;
import java.util.ArrayList;

/**
 * Created by Manish-Pc on 12/12/2017.
 */

public class PaymentReceivedAdapter extends ArrayAdapter{

    ArrayList<PaymentHistoryData> paymentHistoryList;
    Context context;
    int resource;

    public PaymentReceivedAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList<PaymentHistoryData> paymentHistoryList) {
        super(context, resource, paymentHistoryList);
        this.context = context;
        this.resource = resource;
        this.paymentHistoryList = paymentHistoryList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        PaymentHistoryData paymentHistoryData = paymentHistoryList.get(position);

        Log.d("SIY", "Name : "+paymentHistoryData.getReceiverName());

        View view = LayoutInflater.from(context).inflate(resource, parent, false);
        ImageView paymentReceiverImage = (ImageView) view.findViewById(R.id.payment_user_image);
        TextView paymentReceiverName = (TextView) view.findViewById(R.id.payment_receiver_name);
        TextView paymentReceiverNumber = (TextView)view.findViewById(R.id.payment_receiver_number);
        TextView paymentReceivingDate = (TextView) view.findViewById(R.id.payment_received_date);
        TextView paymentAmountReceived = (TextView) view.findViewById(R.id.payment_amount_received);



        paymentReceiverImage.setImageResource(R.drawable.splash_logo);
        paymentReceiverName.setText(paymentHistoryData.getReceiverName());
        paymentReceiverNumber.setText(paymentHistoryData.getReceiverContact());
        paymentReceivingDate.setText(paymentHistoryData.getReceivingDate());
        paymentAmountReceived.setText(paymentHistoryData.getReceivingAmount());
        return view;
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return paymentHistoryList.get(position);
    }
}

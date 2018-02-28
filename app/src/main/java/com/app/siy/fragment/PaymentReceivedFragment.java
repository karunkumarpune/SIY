package com.app.siy.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.app.siy.R;
import com.app.siy.activity.recorder.PaymentReceived;
import com.app.siy.adapter.PaymentReceivedAdapter;
import com.app.siy.model.PaymentHistoryData;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PaymentReceivedFragment extends Fragment implements AdapterView.OnItemClickListener{


    ListView listView;
    PaymentReceivedAdapter adapter;
    ArrayList<PaymentHistoryData> paymentHistoryList;

    public PaymentReceivedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        paymentHistoryList = new ArrayList<>();

        PaymentHistoryData data1 = new PaymentHistoryData("Aakash", "From 88XXXXXX08", "July 14, 02:50 PM", "$230");
        paymentHistoryList.add(data1);

        PaymentHistoryData data2 = new PaymentHistoryData("John", "From 90XXXXXX32", "Sep 28, 06:26 PM", "$138");
        paymentHistoryList.add(data2);

        PaymentHistoryData data3 = new PaymentHistoryData("Robert", "From 67XXXXXX00", "Feb 22, 03:30 AM", "$560");
        paymentHistoryList.add(data3);

        PaymentHistoryData data4 = new PaymentHistoryData("Kevin", "From 89XXXXXX11", "Apr 12, 02:59 PM", "$250");
        paymentHistoryList.add(data4);

        PaymentHistoryData data5 = new PaymentHistoryData("Aakash", "From 88XXXXXX08", "July 14, 02:50 PM", "$230");
        paymentHistoryList.add(data5);

        PaymentHistoryData data6 = new PaymentHistoryData("John", "From 90XXXXXX32", "Sep 28, 06:26 PM", "$138");
        paymentHistoryList.add(data6);

        PaymentHistoryData data7 = new PaymentHistoryData("Robert", "From 67XXXXXX00", "Feb 22, 03:30 AM", "$560");
        paymentHistoryList.add(data7);

        PaymentHistoryData data8 = new PaymentHistoryData("Kevin", "From 89XXXXXX11", "Apr 12, 02:59 PM", "$250");
        paymentHistoryList.add(data8);

        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("SIY", "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.recorder_fragment_payment_received, container, false);
        listView = (ListView) view.findViewById(R.id.list_view_payment_received);

        adapter = new PaymentReceivedAdapter(getActivity(), R.layout.recorder_layout_payment_received, paymentHistoryList);
        Log.d("SIY", "data"+paymentHistoryList.get(0).getReceiverName());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getContext(), PaymentReceived.class);
        intent.putExtra("PAYMENT_DETAILS", paymentHistoryList.get(position));
        startActivity(intent);
    }
}

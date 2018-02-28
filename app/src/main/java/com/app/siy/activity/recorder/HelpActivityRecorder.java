package com.app.siy.activity.recorder;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.utils.AppUtils;

public class HelpActivityRecorder extends AppCompatActivity implements View.OnClickListener {


    private Toolbar toolbar;
    private ImageView backImage;
    private EditText etHelp;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explorer_activity_help);

        toolbar = (Toolbar) findViewById(R.id.include_toolbar_help_layout);
        TextView textView = toolbar.findViewById(R.id.tool_bar_text);
        textView.setText("Help");

        backImage = toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);

        etHelp = (EditText) findViewById(R.id.et_help);
        btnSend = (Button) findViewById(R.id.btn_send_help);
        btnSend.setOnClickListener(this);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;

            case R.id.btn_send_help:

                String helpText = etHelp.getText().toString();
                if (TextUtils.isEmpty(helpText)) {
                    AppUtils.showToastBlack(this, "Please enter your message");
                } else {
                    showConfirmationDialog();
                }

                break;
        }
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_help_confirmation, null);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        TextView btnCancel = view.findViewById(R.id.tv_cancel_help_dialog);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        TextView btnYes = view.findViewById(R.id.tv_yes_help_dialog);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.showToastBlack(HelpActivityRecorder.this, "Your request has been sent");
                Intent intent = new Intent(HelpActivityRecorder.this, RecorderHomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}
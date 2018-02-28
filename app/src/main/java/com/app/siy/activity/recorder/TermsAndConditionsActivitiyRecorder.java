package com.app.siy.activity.recorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.siy.R;

public class TermsAndConditionsActivitiyRecorder extends AppCompatActivity {


    private Toolbar toolbar;
    private ImageView backImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recorder_activity_terms_and_conditions);

        toolbar = (Toolbar) findViewById(R.id.include_toolbar_terms_and_conditions);
        TextView toolbarText = toolbar.findViewById(R.id.tool_bar_text);
        toolbarText.setText("Terms and conditions");

        backImage = toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}

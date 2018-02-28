package com.app.siy.activity.recorder;

import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.siy.R;

public class RecorderTaskListActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressBar progressBar;
    private Toolbar toolbar;
    private ImageView backImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recorder_activity_task_list);
        toolbar = (Toolbar) findViewById(R.id.include_toolbar_task_list);

        progressBar = (ProgressBar) findViewById(R.id.progress_recorder);
        //Make ProgressBar Solid.
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_IN);


        TextView textView = toolbar.findViewById(R.id.tool_bar_text);
        textView.setText("Task List");
        backImage = (ImageView) toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

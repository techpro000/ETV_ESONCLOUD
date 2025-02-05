package com.etv.task.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.etv.activity.BaseActivity;
import com.etv.activity.MainActivity;
import com.etv.config.AppConfig;
import com.etv.task.shiwei.PlaySingleShiWeiActivity;

import java.util.List;

public class TaskBlackActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        initView();
    }

    private void initView() {
        Intent intent = new Intent();
        intent.setClass(TaskBlackActivity.this, PlaySingleActivity.class);
        startActivity(intent);
        finish();
    }

}

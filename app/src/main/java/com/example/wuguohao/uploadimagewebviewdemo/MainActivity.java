package com.example.wuguohao.uploadimagewebviewdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

/**
 * Created by wuguohao on 17/5/25.
 */

public class MainActivity extends Activity {
    Button mButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                intent.putExtra(WebViewActivity.URL_EXTRA_NAME_STR, "https://visitor.zhimahezi.net/#/");
                startActivity(intent);
            }
        });
    }
}

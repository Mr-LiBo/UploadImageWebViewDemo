package com.example.wuguohao.uploadimagewebviewdemo;

import android.content.Intent;

/**
 * Created by wuguohao on 17/5/25.
 */

public class TestActivity extends WebViewActivity
{

    @Override
    public String getUrl() {
        Intent intent = getIntent();
        return intent.getStringExtra(WebViewActivity.URL_EXTRA_NAME_STR);
    }
}

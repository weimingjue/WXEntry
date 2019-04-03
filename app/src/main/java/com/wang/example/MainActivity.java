package com.wang.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.wang.wxentry.WXEntryUtils;
import com.wang.wxentry.wx.WXEntryActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WXEntryUtils.setWXAppId("123456");
        WXEntryActivity.WXLogin(this, new WXEntryActivity.OnWXListener() {
            @Override
            public void onWXSuccess(BaseResp resp) {
                //成功
            }

            @Override
            public void onError(int errType, String errorMsg, @Nullable BaseResp resp) {
                //失败详见errorMsg
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

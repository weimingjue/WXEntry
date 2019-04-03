package com.wang.wxentry.wx;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.wang.wxentry.Interfaceabstract.OnCancelClickListener;
import com.wang.wxentry.WXEntryUtils;
import com.wang.wxentry.bean.WXCode;
import com.wang.wxentry.utils.TimeUtil;

import java.lang.ref.WeakReference;

/**
 * 已经解耦,请使用{@link #WXPay}
 */
public class WXPayEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {

    private IWXAPI api;

    private static WXEntryActivity.OnWXListener mListener = null;
    private static WeakReference<Activity> mActivity = null;
    private static int mTime = WXEntryActivity.DEFAULT_WXTIME;
    private static OnCancelClickListener<Void> mTimeListener = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, WXEntryUtils.appId);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
        if (mListener != null) {
            mListener.onReq(req);
            mListener = null;
        }
        finish();
    }

    @Override
    public void onResp(BaseResp resp) {
        if (mListener != null) {
            mListener.onResp(resp);
            mListener = null;
        }
        finish();
    }

    public static void setResultListener(Activity activity, WXEntryActivity.OnWXListener listener) {
        if (mListener != null) {
            mListener.onError(WXEntryActivity.OnWXListener.TYPE_REPLACE, "支付变更！", null);
        }
        mActivity = new WeakReference<>(activity);
        mListener = listener;
        if (mTimeListener == null) {
            mTimeListener = new OnCancelClickListener<Void>() {
                @Override
                public void clickCancel(Void aVoid) {
                    mTime--;
                    Activity ac = mActivity.get();
                    if (ac == null || ac.isFinishing()) {
                        clear();
                    }
                    if (mTime < 0) {
                        if (mListener != null) {
                            mListener.onError(WXEntryActivity.OnWXListener.TYPE_TIMEOUT, "支付超时！", null);
                        }
                        clear();
                    }
                }

                void clear() {
                    TimeUtil.removeTimeListener(this);
                    mActivity = null;
                    mListener = null;
                    mTime = WXEntryActivity.DEFAULT_WXTIME;
                }
            };
        }
        mTime = WXEntryActivity.DEFAULT_WXTIME;
        TimeUtil.addTimeListener(mTimeListener);
    }

    /**
     * 把微信支付解耦,只需要调这个方法即可
     */
    public static void WXPay(Activity activity, WXCode wc, WXEntryActivity.OnWXListener listener) {
        setResultListener(activity, listener);
        IWXAPI api = WXAPIFactory.createWXAPI(activity.getApplicationContext(), WXEntryUtils.appId);
        api.registerApp(WXEntryUtils.appId);
        if (!api.isWXAppInstalled()) {
            Toast.makeText(activity, "您可能没有安装微信!", Toast.LENGTH_SHORT).show();
        }
        PayReq request = new PayReq();
        request.appId = WXEntryUtils.appId;
        request.partnerId = wc.mch_id;
        request.prepayId = wc.prepay_id;
        request.packageValue = "Sign=WXPay";
        request.nonceStr = wc.nonce_str;
        request.timeStamp = String.valueOf(wc.timestamp);
        request.sign = wc.paySign;
        api.sendReq(request);
    }
}
package com.wang.wxentry.wx;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.wang.wxentry.Interfaceabstract.OnCancelClickListener;
import com.wang.wxentry.WXEntryUtils;
import com.wang.wxentry.utils.TimeUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * 已经解耦,请使用{@link #WXLogin}
 */
public class WXEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {

    private IWXAPI api;

    private static OnWXListener mListener = null;
    private static WeakReference<Activity> mActivity = null;
    public static final int DEFAULT_WXTIME = 300;
    private static int mTime = DEFAULT_WXTIME;
    private static OnCancelClickListener<Void> mTimeListener = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, WXEntryUtils.appId);
        api.handleIntent(getIntent(), this);
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    public void onReq(BaseReq req) {
        if (mListener != null) {
            mListener.onReq(req);
            mListener = null;
        }
        finish();
    }

    public void onResp(BaseResp resp) {
        if (mListener != null) {
            mListener.onResp(resp);
            mListener = null;
        }
        finish();
    }

    public static void setResultListener(Activity activity, OnWXListener listener) {
        if (mListener != null) {
            mListener.onError(OnWXListener.TYPE_REPLACE, "支付变更！", null);
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
                            mListener.onError(OnWXListener.TYPE_TIMEOUT, "支付超时！", null);
                        }
                        clear();
                    }
                }

                void clear() {
                    TimeUtil.removeTimeListener(this);
                    mActivity = null;
                    mListener = null;
                }
            };
        }
        mTime = DEFAULT_WXTIME;
        TimeUtil.addTimeListener(mTimeListener);
    }

    /**
     * 把微信授权解耦,只需要调这个方法即可
     */
    public static void WXLogin(Activity activity, OnWXListener listener) {
        setResultListener(activity, listener);
        IWXAPI api = WXAPIFactory.createWXAPI(activity, WXEntryUtils.appId, true);
        api.registerApp(WXEntryUtils.appId);
        if (!api.isWXAppInstalled()) {
            Toast.makeText(activity, "您可能没有安装微信!", Toast.LENGTH_SHORT).show();
        }
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        api.sendReq(req);
    }

    public static abstract class OnWXListener {
        //错误类型:请求超时；没有回调并被新的分享或支付替换；微信的错误
        public static final int TYPE_TIMEOUT = 0, TYPE_REPLACE = 1, TYPE_WX = 2;

        @IntDef({TYPE_TIMEOUT, TYPE_REPLACE, TYPE_WX})
        @Retention(RetentionPolicy.SOURCE)
        public @interface type {
        }//该变量只能传入上面几种,否则会报错

        //微信主动调app
        public void onReq(BaseReq req) {
        }

        /**
         * app调授权支付等，此处对resp进行了判断，分别拆分到了{@link #onWXSuccess}{@link #onError}
         */
        public void onResp(BaseResp resp) {
            switch (resp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    onWXSuccess(resp);
                    break;
                case BaseResp.ErrCode.ERR_COMM:
                    onError(TYPE_WX, "通信失败！", resp);
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL:
                    onError(TYPE_WX, "取消支付！", resp);
                    break;
                case BaseResp.ErrCode.ERR_SENT_FAILED:
                    onError(TYPE_WX, "发送失败！", resp);
                    break;
                case BaseResp.ErrCode.ERR_AUTH_DENIED:
                    onError(TYPE_WX, "身份验证失败！", resp);
                    break;
                case BaseResp.ErrCode.ERR_UNSUPPORT:
                    onError(TYPE_WX, "暂不支持此方式！", resp);
                    break;
                case BaseResp.ErrCode.ERR_BAN:
                    onError(TYPE_WX, "支付被禁止！", resp);
                    break;
                default:
                    onError(TYPE_WX, "其他支付错误！", resp);
                    break;
            }
        }

        public abstract void onWXSuccess(BaseResp resp);

        /**
         * @param errType  错误类型：超时、替换、微信错误
         * @param errorMsg 错误原因
         * @param resp     当微信返回错误的时候不是null
         */
        public abstract void onError(@type int errType, String errorMsg, @Nullable BaseResp resp);
    }
}

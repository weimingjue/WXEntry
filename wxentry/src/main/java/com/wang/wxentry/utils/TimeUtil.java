package com.wang.wxentry.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.MainThread;

import com.wang.wxentry.Interfaceabstract.OnCancelClickListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 增加了倒计时handler,每隔一秒会回调一次,减少重复new handler
 */
public final class TimeUtil {
    //管理所有的倒计时，防止创建过多的handler
    private static final ArrayList<WeakReference<OnCancelClickListener<Void>>> mTimeListeners = new ArrayList<>();
    /**
     * 可以公共使用post相关方法
     */
    @SuppressLint("HandlerLeak")//本代码不会出现内存泄漏,使用post时需要注意内存泄漏
    public static final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mTimeListeners.size() == 0) return;
//            此处为当所有Activity都退出时停止倒计时
//            if (MyLibsApp.mIsAcitivytDestroy) {
//                mTimeListeners.clear();
//                mHandler.removeMessages(1);
//                return;
//            }
            for (int i = mTimeListeners.size() - 1; i >= 0; i--) {
                OnCancelClickListener<Void> timeListener = mTimeListeners.get(i).get();
                if (timeListener == null) {
                    mTimeListeners.remove(i);
                } else {
                    timeListener.clickCancel(null);
                }
            }
            sendEmptyMessageDelayed(1, 1000);
        }
    };

    //为倒计时添加回调
    @MainThread
    public static void addTimeListener(OnCancelClickListener<Void> timeListener) {
        if (timeListener != null) {
            for (WeakReference<OnCancelClickListener<Void>> wr : mTimeListeners)
                if (timeListener == wr.get())
                    return;

            if (mTimeListeners.size() == 0) {
                mHandler.removeMessages(1);
                mHandler.sendEmptyMessageDelayed(1, 1000);
            }

            mTimeListeners.add(new WeakReference<>(timeListener));
        }
    }

    //删除所有的倒计时
    @MainThread
    public static void removeTimeListener() {
        mTimeListeners.clear();
    }

    //删除倒计时
    @MainThread
    public static boolean removeTimeListener(OnCancelClickListener<Void> timeListener) {
        if (mTimeListeners.size() == 0 || timeListener == null) return true;
        for (int i = 0; i < mTimeListeners.size(); i++) {
            if (mTimeListeners.get(i).get() == timeListener) {
                mTimeListeners.remove(i);
                return true;
            }
        }
        return false;
    }
}
package com.wang.wxentry.Interfaceabstract;

/**
 * 主要是解决自定义view回调命名繁琐的问题
 * 实现类:自己写,很简单,把那些乱七八糟的回调名改掉,例:
 * public void setOnItemClickListener(OnOkClickListener<Void, Integer> listener) {
 * mListener = listener;
 * }
 */
public interface OnCancelClickListener<OBJ> {
    void clickCancel(OBJ obj);
}

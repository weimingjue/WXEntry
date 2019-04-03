package com.wang.wxentry.bean;

import java.io.Serializable;

public class WXCode implements Serializable {

    public String nonce_str;
    public String paySign;
    public String appid;
    public String sign;
    public String trade_type;
    public String return_msg;
    public String result_code;
    public String mch_id;
    public String return_code;
    public String prepay_id;
    public int timestamp;
}

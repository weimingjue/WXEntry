# demo，可以下载下来自行参考

```
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
```
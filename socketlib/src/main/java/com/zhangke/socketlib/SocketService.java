package com.zhangke.socketlib;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Socket连接服务，负责Socket连接，数据传输等操作
 * Created by ZhangKe on 2018/6/7.
 */
public class SocketService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}

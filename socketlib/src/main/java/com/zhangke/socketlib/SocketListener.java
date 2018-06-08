package com.zhangke.socketlib;

/**
 * Socket监听器
 * Created by ZhangKe on 2018/6/8.
 */
public interface SocketListener {

    void onConnected();

    void onConnectError(Throwable cause);

    void onDisconnected();

    void onSendTextError(Throwable cause);

    void onTextMessage(String message);
}

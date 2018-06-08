package com.zhangke.socketlib;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zhangke.zlog.ZLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Socket连接服务，负责Socket连接，数据传输等操作
 * Created by ZhangKe on 2018/6/7.
 */
public class SocketService extends Service implements SocketListener {

    private static final String TAG = "SocketLib";

    private SocketThread mSocketThread;
    private Handler socketHandler;

    private List<SocketListener> listeners = new ArrayList<>();

    private SocketService.ServiceBinder serviceBinder = new SocketService.ServiceBinder();

    public class ServiceBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (serviceBinder == null) {
            serviceBinder = new SocketService.ServiceBinder();
        }
        ZLog.i(TAG, "onBind");
        return serviceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        start();
    }

    public void send(String text) {
        mSocketThread.send(text);
    }

    public void addListener(SocketListener listener){
        listeners.add(listener);
    }

    @Override
    public void onConnected() {
        ZLog.d(TAG, "Socket 已连接");
        for(SocketListener listener : listeners){
            listener.onConnected();
        }
    }

    @Override
    public void onConnectError(Throwable cause) {
        ZLog.d(TAG, String.format("Socket 连接失败：%s", cause.toString()));
        for(SocketListener listener : listeners){
            listener.onConnectError(cause);
        }
    }

    @Override
    public void onDisconnected() {
        ZLog.d(TAG, "Socket 断开连接");
        for(SocketListener listener : listeners){
            listener.onDisconnected();
        }
    }

    @Override
    public void onSendTextError(Throwable cause) {
        ZLog.d(TAG, String.format("Socket 数据发送失败：%s", cause.toString()));
        for(SocketListener listener : listeners){
            listener.onSendTextError(cause);
        }
    }

    @Override
    public void onTextMessage(String message) {
        ZLog.d(TAG, "Socket 收到消息：" + message);
        for(SocketListener listener : listeners){
            listener.onTextMessage(message);
        }
    }

    private void start() {
        if (mSocketThread == null || !mSocketThread.isAlive()) {
            ZLog.d(TAG, "正在创建Socket线程并初始化");
            mSocketThread = new SocketThread();
            mSocketThread.start();
            socketHandler = mSocketThread.getHandler();
            ZLog.d(TAG, "Socket初始化完成");
        }
        socketHandler.sendEmptyMessage(MessageType.CONNECT);
    }

    private void quit() {
        ZLog.d(TAG, "正在结束Socket");
        socketHandler.sendEmptyMessage(MessageType.QUIT);
        mSocketThread.interrupt();
    }

    @Override
    public void onDestroy() {
        quit();
        super.onDestroy();
    }
}

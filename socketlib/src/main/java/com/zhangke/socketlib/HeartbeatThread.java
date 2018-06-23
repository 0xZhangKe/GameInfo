package com.zhangke.socketlib;

import android.os.Handler;
import android.os.Message;

import com.zhangke.zlog.ZLog;

import java.net.Socket;

/**
 * 用于发送心跳包
 * Created by ZhangKe on 2018/6/10.
 */
public class HeartbeatThread extends Thread {

    private static final String TAG = "SocketLib";

    /**
     * 心跳包间隔
     */
    private final int INTERVAL = 5000;

    private Socket mSocket;
    private Handler mHandler;

    private boolean stop;

    public HeartbeatThread(Handler handler) {
        this.mHandler = handler;
        stop = false;
    }

    public void bindSocket(Socket socket) {
        this.mSocket = socket;
    }

    @Override
    public void run() {
        super.run();
        while (!stop) {
            if (mSocket != null && mSocket.isConnected() && !mSocket.isClosed() && mHandler != null) {
                Message message = mHandler.obtainMessage();
                message.what = MessageType.SEND_MESSAGE;
                message.obj = "hello";
                mHandler.sendMessage(message);
            }
            try {
                Thread.sleep(INTERVAL);
            } catch (InterruptedException e) {
                ZLog.e(TAG, "HeartbeatThread#run()", e);
                if (stop) {
                    quit();
                } else {
                    if (Thread.currentThread().isInterrupted()) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    public void quit() {
        stop = true;
        mSocket = null;
        mHandler = null;
        this.interrupt();
        ZLog.d(TAG, "心跳线程已停止");
    }
}

package com.zhangke.socketlib;

import android.os.Handler;

import com.zhangke.zlog.ZLog;

import java.net.Socket;

/**
 * 守护线程，判断Socket是否断开连接，断开则重新连接
 * Created by ZhangKe on 2018/6/20.
 */
public class DaemonThread extends Thread {

    private static final String TAG = "SocketLib";

    private Socket mSocket;
    private Handler mHandler;
    private boolean stop;

    public DaemonThread(Handler mHandler) {
        this.mHandler = mHandler;
        stop = false;
    }

    public void bindSocket(Socket socket) {
        this.mSocket = socket;
    }

    @Override
    public void run() {
        super.run();
        while (!stop) {
            try {
                if(mSocket != null && mSocket.isConnected() && mSocket.isClosed()&& mHandler != null){
                    mHandler.sendEmptyMessage(MessageType.CONNECT);
                }
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                ZLog.e(TAG, "DaemonThread#run()", e);
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
        DaemonThread.this.interrupt();
        ZLog.d(TAG, "守护线程已停止");
    }
}

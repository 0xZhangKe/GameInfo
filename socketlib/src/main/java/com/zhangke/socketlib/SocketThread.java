package com.zhangke.socketlib;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.zhangke.zlog.ZLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Socket线程
 * Created by ZhangKe on 2018/6/7.
 */
public class SocketThread extends Thread {

    private static final String TAG = "SocketLib";

    /**
     * 0-未连接
     * 1-正在连接
     * 2-已连接
     */
    private int status = 0;

    private SocketHandler mHandler;

    private Socket mSocket;
    private OutputStream mOutputStream;
    private InputMonitorThread mInputMonitorThread;

    public SocketThread() {
    }

    @Override
    public void run() {
        super.run();
        mHandler = new SocketHandler();
        Looper.prepare();
        Looper.loop();
    }

    public Handler getHandler() {
        return mHandler;
    }

    private void connect() {
        try {
            mSocket = new Socket("", 1234);
            mInputMonitorThread = new InputMonitorThread(mSocket, mHandler);
            mInputMonitorThread.start();
        } catch (IOException e) {
            ZLog.e(TAG, "connect()", e);
        }
    }

    private void sendText(String text) {
        try {
            if (mOutputStream == null) {
                mOutputStream = mSocket.getOutputStream();
            }
            mOutputStream.write((text + "\n").getBytes("utf-8"));
            mOutputStream.flush();
        } catch (IOException e) {
            ZLog.e(TAG, "sendText(String)", e);
        }
    }

    private void disconnect() {
        try {
            mInputMonitorThread.quit();
            mInputMonitorThread.interrupt();
            mOutputStream.close();
            mSocket.close();
        } catch (IOException e) {
            ZLog.e(TAG, "disconnect()", e);
        }
    }

    private static class SocketHandler extends Handler {

        SocketHandler() {

        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    private static class InputMonitorThread extends Thread {

        private Socket mSocket;
        private Handler mHandler;

        private boolean stop;

        private InputStream mInputStream;
        private InputStreamReader mInputStreamReader;
        private BufferedReader mBufferedReader;

        InputMonitorThread(Socket mSocket, Handler handler) {
            this.mSocket = mSocket;
            this.mHandler = handler;
            stop = false;
        }

        @Override
        public void run() {
            super.run();
            while (!stop) {
                try {
                    mInputStream = mSocket.getInputStream();
                    mInputStreamReader = new InputStreamReader(mInputStream);
                    mBufferedReader = new BufferedReader(mInputStreamReader);
                    String response = mBufferedReader.readLine();
                    if (!TextUtils.isEmpty(response)) {
                        ZLog.i(TAG, "run()——>收到消息：" + response);
                        Message message = mHandler.obtainMessage();
                        message.what = MessageType.RECEIVE_MESSAGE.ordinal();
                        message.obj = response;
                        mHandler.sendMessage(message);
                    }
                } catch (IOException e) {
                    ZLog.e(TAG, "InputObserver#run()", e);
                }
            }
        }

        void quit() {
            stop = true;
            try {
                mInputStream.close();
                mInputStreamReader.close();
                mBufferedReader.close();
            } catch (Exception e) {
                ZLog.e(TAG, "quit()", e);
            }
        }
    }
}

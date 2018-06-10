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
import java.net.SocketException;
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

    private SocketListener socketListener;

    private InputMonitorThread mInputMonitorThread;
    private HeartbeatThread mHeartbeatThread;

    public SocketThread() {
    }

    @Override
    public void run() {
        super.run();
        Looper.prepare();
        mHandler = new SocketHandler();
        status = 0;
        mInputMonitorThread = new InputMonitorThread(mHandler);
        mInputMonitorThread.start();
        mHeartbeatThread = new HeartbeatThread(mHandler);
        mHeartbeatThread.start();
        Looper.loop();
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void setSocketListener(SocketListener socketListener) {
        this.socketListener = socketListener;
    }

    private class SocketHandler extends Handler {

        SocketHandler() {

        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MessageType.CONNECT:
                    connect();
                    break;
                case MessageType.DISCONNECT:
                    disconnect();
                    break;
                case MessageType.QUIT:
                    quit();
                    break;
                case MessageType.SEND_MESSAGE:
                    if (msg.obj instanceof String) {
                        if (mSocket.isConnected() && !mSocket.isClosed()) {
                            sendText((String) msg.obj);
                        } else {
                            mHandler.sendEmptyMessage(MessageType.CONNECT);
                            mHandler.sendMessage(msg);
                        }
                    }
                    break;
                case MessageType.RECEIVE_MESSAGE:
                    if (msg.obj instanceof String && socketListener != null) {
                        socketListener.onTextMessage((String) msg.obj);
                    }
                    break;
            }
        }

        /**
         * 连接服务
         */
        private void connect() {
            if (status == 2) {
                ZLog.d(TAG, "Socket已连接，请勿重复调用");
                return;
            }
            ZLog.d(TAG, "开始连接Socket...");
            status = 1;
            try {
                mSocket = new Socket("192.168.31.78", 6800);
                mInputMonitorThread.bindSocket(mSocket);
                mHeartbeatThread.bindSocket(mSocket);
                status = 2;
                if (socketListener != null) {
                    socketListener.onConnected();
                }
                ZLog.d(TAG, "Socket连接成功");
            } catch (IOException e) {
                ZLog.i(TAG, "Socket连接失败", e);
                ZLog.e(TAG, "Socket连接失败", e);
                if (socketListener != null) {
                    socketListener.onConnectError(e);
                }
            }
        }

        /**
         * 断开连接
         */
        private void disconnect() {
            if (status == 0) {
                ZLog.d(TAG, "Socket未连接，请勿重复调用");
                return;
            }
            ZLog.d(TAG, "正在断开Socket连接...");
            try {
                if (mSocket != null) {
                    mSocket.close();
                }
                status = 0;
                if (mOutputStream != null) {
                    mOutputStream.close();
                    mOutputStream = null;
                }
                if (socketListener != null) {
                    socketListener.onDisconnected();
                }
                ZLog.i(TAG, "Socket连接已断开");
            } catch (IOException e) {
                ZLog.i(TAG, "断开Socket连接失败", e);
                ZLog.e(TAG, "disconnect()", e);
            }
        }

        /**
         * 结束该线程
         */
        private void quit() {
            ZLog.d(TAG, "正在结束Socket线程");
            disconnect();
            Looper looper = Looper.myLooper();
            if (looper != null) {
                looper.quit();
            }
            status = 0;
            if (mInputMonitorThread != null) {
                mInputMonitorThread.quit();
                mInputMonitorThread = null;
            }
            if (mHeartbeatThread != null) {
                mHeartbeatThread.quit();
                mHeartbeatThread = null;
            }
            ZLog.d(TAG, "Socket线程已结束");
        }

        private void sendText(String text) {
            try {
                if (mSocket.isConnected() && !mSocket.isClosed()) {
                    if (mOutputStream == null) {
                        mOutputStream = mSocket.getOutputStream();
                    }
                    mOutputStream.write((text + "\n").getBytes("utf-8"));
                    mOutputStream.flush();
                    ZLog.i(TAG, String.format("数据：%s,已发送", text));
                }
            } catch (IOException e) {
                ZLog.i(TAG, String.format("数据：%s,发送失败", text), e);
                ZLog.e(TAG, "sendText(String)", e);
                if (socketListener != null) {
                    socketListener.onSendTextError(e);
                }
            }
        }
    }
}

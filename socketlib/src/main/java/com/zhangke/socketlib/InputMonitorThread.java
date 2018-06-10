package com.zhangke.socketlib;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.zhangke.zlog.ZLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

/**
 * 读取Socket数据线程，
 * 负责读取数据，判断Socket是否已关闭，关闭则通知SocketThread
 * Created by ZhangKe on 2018/6/10.
 */
public class InputMonitorThread extends Thread {

    private static final String TAG = "SocketLib";

    private boolean stop;

    private InputStream mInputStream;
    private InputStreamReader mInputStreamReader;
    private BufferedReader mBufferedReader;

    private Socket mSocket;
    private Handler mHandler;

    /**
     * 断开连接消息是否已发送，
     * 检测到连接断开只发送一次即可。
     */
    private boolean closeMessageIsSend = false;

    public InputMonitorThread(Handler handler) {
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
            try {
                if (mSocket != null) {
                    if (mSocket.isClosed()) {
                        //连接已关闭
                        if(!closeMessageIsSend){
                            closeMessageIsSend = true;
                            destroyIORes();
                            mHandler.sendEmptyMessage(MessageType.DISCONNECT);
                        }
                    } else {
                        closeMessageIsSend = false;
                        if (mInputStream == null) {
                            mInputStream = mSocket.getInputStream();
                        }
                        mInputStreamReader = new InputStreamReader(mInputStream);
                        mBufferedReader = new BufferedReader(mInputStreamReader);
                        String response = mBufferedReader.readLine();
                        if (!TextUtils.isEmpty(response)) {
                            ZLog.i(TAG, "run()——>Socket收到消息：" + response);
                            Message message = mHandler.obtainMessage();
                            message.what = MessageType.RECEIVE_MESSAGE;
                            message.obj = response;
                            mHandler.sendMessage(message);
                        } else {
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                ZLog.e(TAG, "InputMonitorThread#run()", e);
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
                }
            } catch (SocketException e) {
                //连接已关闭
                destroyIORes();
                mHandler.sendEmptyMessage(MessageType.DISCONNECT);
            } catch (IOException e) {
                ZLog.e(TAG, "InputMonitorThread#run()", e);
            }
        }
    }

    /**
     * 销毁IO资源，当Socket连接被关闭时应当调用
     */
    private void destroyIORes() {
        try {
            mInputStream.close();
            mInputStream = null;
            mInputStreamReader.close();
            mInputStreamReader = null;
            mBufferedReader.close();
            mBufferedReader = null;
        } catch (Exception e) {
            ZLog.e(TAG, "destroyIORes()", e);
        }
    }

    /**
     * 用于关闭线程，只有SocketThread关闭时才应当关闭次线程。
     */
    public void quit() {
        stop = true;
        destroyIORes();
        mSocket = null;
        mHandler = null;
        Thread.currentThread().interrupt();
        ZLog.i(TAG, "Socket数据读取线程已结束");
    }
}

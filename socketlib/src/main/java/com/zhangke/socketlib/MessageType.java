package com.zhangke.socketlib;

/**
 * SocketThread中的消息类型
 * Created by ZhangKe on 2018/6/7.
 */
public enum MessageType {

    CONNECT,//连接Socket
    DISCONNECT,//断开连接
    QUIT,//结束线程
    SEND_MESSAGE,//通过Socket连接发送数据
    RECEIVE_MESSAGE//通过Socket获取到数据
}

package com.zhangke.gameinfo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.zhangke.socketlib.SocketListener;
import com.zhangke.socketlib.SocketService;
import com.zhangke.zlog.ZLog;

public class MainActivity extends AppCompatActivity implements SocketListener {

    private static final String TAG = "MainActivity";

    private SocketService mSocketService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSocketService = (SocketService) ((SocketService.ServiceBinder) service).getService();
            mSocketService.addListener(MainActivity.this);
            mSocketService.connect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ZLog.e(TAG, "SocketService连接失败");
        }
    };

    private EditText etText;
    private TextView tvReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etText = findViewById(R.id.et_text);
        tvReceive = findViewById(R.id.tv_receive);

        findViewById(R.id.btn_send).setOnClickListener(v -> {
            mSocketService.send(etText.getText().toString());
        });

        findViewById(R.id.btn_connect).setOnClickListener(v -> {
            runOnUiThread(() -> tvReceive.setText(String.format("%s\n正在连接...", tvReceive.getText().toString())));
            mSocketService.connect();
        });

        findViewById(R.id.btn_disconnect).setOnClickListener(v -> {
            mSocketService.disconnect();
        });

        bindSocketService();
    }

    private void bindSocketService() {
        Intent intent = new Intent(this, SocketService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unBindSocketService() {
        unbindService(serviceConnection);
    }

    @Override
    public void onConnected() {
        runOnUiThread(() -> tvReceive.setText(String.format("%s\n连接成功", tvReceive.getText().toString())));
    }

    @Override
    public void onConnectError(Throwable cause) {
        runOnUiThread(() -> tvReceive.setText(String.format("%s\n连接失败", tvReceive.getText().toString())));
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> tvReceive.setText(String.format("%s\n断开连接", tvReceive.getText().toString())));
    }

    @Override
    public void onSendTextError(Throwable cause) {
        runOnUiThread(() -> tvReceive.setText(String.format("%s\n数据发送失败：%s", tvReceive.getText().toString(), cause.toString())));
    }

    @Override
    public void onTextMessage(String message) {
        runOnUiThread(() -> tvReceive.setText(String.format("%s\n接收到消息：%s", tvReceive.getText().toString(), message)));
    }

    @Override
    protected void onDestroy() {
        unBindSocketService();
        super.onDestroy();
    }
}

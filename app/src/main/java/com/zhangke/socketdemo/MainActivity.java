package com.zhangke.socketdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.zhangke.socketlib.SocketListener;
import com.zhangke.socketlib.SocketService;
import com.zhangke.zlog.ZLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SocketListener {

    private static final String TAG = "MainActivity";

    private ArrayList<GameInfo> gameInfoList = new ArrayList<>();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }

    @Override
    public void onConnectError(Throwable cause) {
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onSendTextError(Throwable cause) {
    }

    @Override
    public void onTextMessage(final String message) {
        runOnUiThread(() -> {
            if (!TextUtils.isEmpty(message) && message.contains("<") && message.contains(">") && message.contains(",")) {
                String messageBody = message.replace(">", "");
                messageBody = messageBody.replace("<", "");
                messageBody = messageBody.replaceAll(" ", "");
                String[] infoArray = messageBody.split(",");
                if (infoArray.length == 3) {
                    String number = infoArray[0];
                    String hitCount = infoArray[1];
                    String beHitCount = infoArray[2];
                    if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(hitCount) && !TextUtils.isEmpty(beHitCount)) {
                        addInfo(new GameInfo(number, Integer.valueOf(hitCount), Integer.valueOf(beHitCount)));
                    }
                }
            }
        });
    }

    private void addInfo(GameInfo gameInfo) {
        if (gameInfoList.contains(gameInfo)) {
            gameInfoList.remove(gameInfo);
        }
        gameInfoList.add(gameInfo);
        Collections.sort(gameInfoList, (GameInfo o1, GameInfo o2) -> o1.getScore() - o2.getScore());
        if (gameInfoList.size() > 6) {
            gameInfoList = (ArrayList<GameInfo>) gameInfoList.subList(0, 6);
        }
    }

    private void showInfo(){

    }

    @Override
    protected void onDestroy() {
        unBindSocketService();
        super.onDestroy();
    }
}

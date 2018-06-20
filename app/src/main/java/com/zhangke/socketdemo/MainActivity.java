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

    private LineView lineView01, lineView02,
            lineView03, lineView04,
            lineView05, lineView06;

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

        lineView01 = findViewById(R.id.linea_01);
        lineView02 = findViewById(R.id.linea_02);
        lineView03 = findViewById(R.id.linea_03);
        lineView04 = findViewById(R.id.linea_04);
        lineView05 = findViewById(R.id.linea_05);
        lineView06 = findViewById(R.id.linea_06);

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
    }

    private void addInfo(GameInfo gameInfo) {
        if (gameInfoList.contains(gameInfo)) {
            gameInfoList.remove(gameInfo);
        }
        gameInfoList.add(gameInfo);
        Collections.sort(gameInfoList, (GameInfo o1, GameInfo o2) -> o2.getScore() - o1.getScore());
        for (int i = 0; i < gameInfoList.size(); i++) {
            gameInfoList.get(i).setRanking(i + 1);
        }
        if (gameInfoList.size() > 6) {
            gameInfoList = new ArrayList<>(gameInfoList.subList(0, 6));
        }
        showInfo();
    }

    private void showInfo() {
        runOnUiThread(() -> {
                    lineView01.setInfo(gameInfoList.size() > 0 ? gameInfoList.get(0) : null);
                    lineView02.setInfo(gameInfoList.size() > 1 ? gameInfoList.get(1) : null);
                    lineView03.setInfo(gameInfoList.size() > 2 ? gameInfoList.get(2) : null);
                    lineView04.setInfo(gameInfoList.size() > 3 ? gameInfoList.get(3) : null);
                    lineView05.setInfo(gameInfoList.size() > 4 ? gameInfoList.get(4) : null);
                    lineView06.setInfo(gameInfoList.size() > 5 ? gameInfoList.get(5) : null);
                }
        );
    }

    @Override
    protected void onDestroy() {
        unBindSocketService();
        super.onDestroy();
    }
}

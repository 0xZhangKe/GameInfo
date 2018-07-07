package com.zhangke.socketdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.VideoView;

import com.zhangke.socketlib.SocketListener;
import com.zhangke.socketlib.SocketService;
import com.zhangke.zlog.ZLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SocketListener {

    private static final String TAG = "MainActivity";

    private TextView tv_title_01, tv_title_02, tv_title_03;
    private LineView lineView01, lineView02,
            lineView03, lineView04,
            lineView05, lineView06;
    private VideoView videoView;

    private ArrayList<GameInfo> gameInfoList = new ArrayList<>();

    private volatile long lastSignalTime;
    private PlayVideoThread playVideoThread;

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
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        tv_title_01 = (TextView) findViewById(R.id.tv_title_01);
        tv_title_02 = (TextView) findViewById(R.id.tv_title_02);
        tv_title_03 = (TextView) findViewById(R.id.tv_title_03);

        lineView01 = findViewById(R.id.linea_01);
        lineView02 = findViewById(R.id.linea_02);
        lineView03 = findViewById(R.id.linea_03);
        lineView04 = findViewById(R.id.linea_04);
        lineView05 = findViewById(R.id.linea_05);
        lineView06 = findViewById(R.id.linea_06);

        videoView = findViewById(R.id.video_view);

        lastSignalTime = System.currentTimeMillis();
        playVideoThread = new PlayVideoThread();
        playVideoThread.start();

        bindSocketService();

        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.trailer));
        videoView.setOnCompletionListener(mp -> {
            playVideo();
        });
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
                    lastSignalTime = System.currentTimeMillis();
                    pauseVideo();
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
                    if (!gameInfoList.isEmpty()) {
                        switch (gameInfoList.size()) {
                            case 1:
                                tv_title_01.setText(gameInfoList.get(0).getNumber());
                                tv_title_02.setText("");
                                tv_title_03.setText("");
                                break;
                            case 2:
                                tv_title_01.setText(gameInfoList.get(0).getNumber());
                                tv_title_02.setText(gameInfoList.get(1).getNumber());
                                tv_title_03.setText("");
                                break;
                            case 3:
                            default:
                                tv_title_01.setText(gameInfoList.get(0).getNumber());
                                tv_title_02.setText(gameInfoList.get(1).getNumber());
                                tv_title_03.setText(gameInfoList.get(2).getNumber());
                                break;
                        }
                    } else {
                        tv_title_01.setText("");
                        tv_title_02.setText("");
                        tv_title_03.setText("");
                    }
                    lineView01.setInfo(gameInfoList.size() > 0 ? gameInfoList.get(0) : null);
                    lineView02.setInfo(gameInfoList.size() > 1 ? gameInfoList.get(1) : null);
                    lineView03.setInfo(gameInfoList.size() > 2 ? gameInfoList.get(2) : null);
                    lineView04.setInfo(gameInfoList.size() > 3 ? gameInfoList.get(3) : null);
                    lineView05.setInfo(gameInfoList.size() > 4 ? gameInfoList.get(4) : null);
                    lineView06.setInfo(gameInfoList.size() > 5 ? gameInfoList.get(5) : null);
                }
        );
    }

    private void playVideo() {
        lastSignalTime = System.currentTimeMillis();
        runOnUiThread(() -> {
            if (videoView.getVisibility() != View.VISIBLE) {
                videoView.setVisibility(View.VISIBLE);
            }
            videoView.start();
            videoView.requestFocus();
        });
    }

    private void pauseVideo() {
        runOnUiThread(() -> {
            if (videoView.isPlaying()) {
                videoView.stopPlayback();
                videoView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoView.getVisibility() == View.VISIBLE) {
            pauseVideo();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unBindSocketService();
        playVideoThread.quit();
        playVideoThread = null;
        super.onDestroy();
    }

    private class PlayVideoThread extends Thread {

        private boolean stop = false;

        @Override
        public void run() {
            super.run();
            while (!stop) {
                if (System.currentTimeMillis() - lastSignalTime > 30000) {
                    playVideo();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    if (stop) {
                        return;
                    } else {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        private void quit() {
            stop = true;
            this.interrupt();
        }
    }
}

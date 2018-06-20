package com.zhangke.socketdemo;

import android.text.TextUtils;

/**
 * Created by ZhangKe on 2018/6/14.
 */
public class GameInfo {

    private int ranking;
    private String number;
    private int hitCount;
    private int beHitCount;

    public GameInfo(String number, int hitCount, int beHitCount) {
        this.number = number;
        this.hitCount = hitCount;
        this.beHitCount = beHitCount;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getHitCount() {
        return hitCount;
    }

    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    public int getBeHitCount() {
        return beHitCount;
    }

    public void setBeHitCount(int beHitCount) {
        this.beHitCount = beHitCount;
    }

    /**
     * 获取积分
     */
    public int getScore(){
        return hitCount - beHitCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof GameInfo)) {
            return false;
        }
        GameInfo gameInfo = (GameInfo) obj;
        return TextUtils.equals(gameInfo.getNumber(), number);
    }

    private volatile int hashCode;

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            if (!TextUtils.isEmpty(number)) {
                result += 31 * result + number.hashCode();
            }
            hashCode = result;
        }
        return result;
    }
}

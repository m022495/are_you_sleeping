package com.google.android.gms.samples.vision.face.facetracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.MediaStore;

/**
 * Created by vkdlv on 2018-04-08.
 */

public class AlarmService extends Service {
    // 얘는 알람으로 구현안하고 미디어 플레이어로 구현

    private MediaPlayer mp;
    private AudioManager ap;
    private int nowVolume;
    private String wake;
    public AlarmService(){

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences opt = getSharedPreferences("Option", MODE_PRIVATE); // 이전거 가져오나봐 자세한건 잘 모르겠다.
        wake = opt.getString("wake","진동"); // 졸음감지 이후 깨우는 방법, 디폴트는 진동
        if(wake.equals("음악")){
            mp = MediaPlayer.create(this, R.raw.ghost1);
        }else if(wake.equals("귀신소리")){
            mp = MediaPlayer.create(this, R.raw.pee);
        }
        ap = (AudioManager) getSystemService(Context.AUDIO_SERVICE); // 얘가 볼륨 조절도 할 수 있음.
        mp.setLooping(true);
        nowVolume = ap.getStreamVolume(AudioManager.STREAM_MUSIC); // 현재 볼륨은 저장
        ap.setStreamVolume(AudioManager.STREAM_MUSIC,15,AudioManager.FLAG_PLAY_SOUND); // 볼륨 최대로!
        mp.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ap.setStreamVolume(AudioManager.STREAM_MUSIC,nowVolume,AudioManager.FLAG_PLAY_SOUND); // 볼륨 다시 원상복귀
        mp.stop();
    }
}

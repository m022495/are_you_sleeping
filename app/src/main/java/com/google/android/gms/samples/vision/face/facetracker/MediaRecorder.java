package com.google.android.gms.samples.vision.face.facetracker;

import java.io.File;
import java.io.IOException;

/**
 * Created by vkdlv on 2018-04-09.
 */

// 녹음기 시작!
public class MediaRecorder {
   private android.media.MediaRecorder mRecorder = null; // 녹음기~

    public void start(){
        try {
        if(mRecorder == null){
            // 포맷이랑 인코더 등등을 설정하고 시작.
            mRecorder = new android.media.MediaRecorder();
            mRecorder.setAudioSource(android.media.MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(android.media.MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            mRecorder.prepare();
            mRecorder.start();
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stop() { // 꺼주는거.
        if(mRecorder != null){
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    // 받은 볼륨 쓰레드로 넘기는 역할.
    public float getAmplitude(){
        if(mRecorder != null){
            return mRecorder.getMaxAmplitude();
        }
        else
            return 0;
    }
}

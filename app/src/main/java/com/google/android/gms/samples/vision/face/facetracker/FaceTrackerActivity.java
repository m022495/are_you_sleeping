/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.face.facetracker;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;

import java.io.IOException;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerActivity extends AppCompatActivity implements SensorEventListener {


    private static final String TAG = "FaceTracker";

    private static CameraSource mCameraSource = null;

    public static CameraSourcePreview mPreview; // 카메라 프리 뷰
    public static  GraphicOverlay mGraphicOverlay; // 프리뷰 위에 그래픽 오버레이를 띄우나봄
    public static boolean isRecorded;

    // 알람을 위한 것들. --> update부분에서는 계속 갱신이 되는 문제 때문에 그냥 전역? 비슷하게 선언.
    public static Intent mute;
    private SharedPreferences.Editor editor;
    public static int isMute; // 처음 시작할 때 음악이 켜져있는 불상사가 있을 경우 바로 끔


    // 데시벨을 위한 것들
    private static MediaRecorder mRecorder; // 목소리 녹음
    private AlertDialog ad; // 다이어 그램인데 일단 아직 안씀.
    private String curVal; // 현재 볼륨
    private static Thread thread; // 데시벨을 듣기위해 쓰레드를 돌림.
    float volume = 10000; // 현재 볼륨을 받기 위해 쓰레드에서 쓰는 것
    public static int CCount; // 눈 깜은 거 측정하는 카운트
    private int OCount; // 눈 뜬거 측정하는 카운트

    private boolean bListener = true;
    private boolean isThreadRun = true;
    private static final int RC_HANDLE_GMS = 9001;

    // 추가된 옵션값
    private String wake;
    private String shut;

    //진동
    public static Vibrator vibrator;
    // sos패턴으로 진동
    int dot = 200;
    int dash = 500;
    int short_gap = 200;
    int medium_gap = 500;
    int long_gap = 1000;
    long[] pattern = {
            0, //즉시시작
            dot, short_gap, dot, short_gap, dot, //s
            medium_gap, dash, short_gap, dash, short_gap, dash, //o
            medium_gap, dot, short_gap, dot, short_gap, dot, //s
            long_gap
    };

    // 흔들기
    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;

    private static final int SHAKE_THRESHOLD = 8000; // 흔드는 속도
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;

    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;
    private int shakeCount = 0;
    private SensorEventListener sensorEventListener;


    // 알림창 띄우기 위한 변수
    private NotificationManager notificationManager; // 알람 관리
    private Notification notification; // 알람
    private RemoteViews notificationView; // 알람 보이도록하는 뷰
    public static boolean isForeGround = true;
    public static Activity activity;

    // Setting창 트리거
    private boolean isSetting = false;

    //trainingmode
    public float leftClosed;
    public float rightClosed;
    public boolean isTrain = false;
    public int TCount = 0;
    public float tLeftClosed = 0;
    public float tRightClosed = 0;
    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        activity = this;
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        leftClosed = 0.5f;
        rightClosed = 0.5f;
        createCameraSource();

        mute = new Intent(this, AlarmService.class);

        SharedPreferences opt = getSharedPreferences("Option", MODE_PRIVATE); // 이전거 가져오나봐 자세한건 잘 모르겠다.
        editor = opt.edit(); // 얘로 처음 시작할 때 isMUte 0으로 설정
        isMute = opt.getInt("isMute", 0); // 0으로 시작해서 혹시모를 서비스를 끌 수 있도록 함.
        mRecorder = new MediaRecorder();
        CCount = 0; // 눈을 감을 때 쓸 카운트
        OCount = 0; // 눈을 뜬 시간을 가지고 올 때 쓰는 카운트
        isRecorded = false;
        // 알림창 생성 맨 아래에 있음

        // 흔들기 센서 초기화
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // 흔들기 센서 초기화1
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                   long currentTime = System.currentTimeMillis();
                    long gabOfTime = (currentTime - lastTime);
                    if (gabOfTime > 100) {
                        lastTime = currentTime;
                        x = event.values[SensorManager.DATA_X];
                        y = event.values[SensorManager.DATA_Y];
                        z = event.values[SensorManager.DATA_Z];

                        speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;

                        lastX = event.values[DATA_X];
                        lastY = event.values[DATA_Y];
                        lastZ = event.values[DATA_Z];
                    }
                    if (speed > SHAKE_THRESHOLD) {
                        shakeCount = shakeCount+1;
                        if(shakeCount>20){
                            shakeCount = 0;
                            if(wake.equals("음악")) {
                                stopService(mute); //꺼줌.
                            }else if(wake.equals("진동")){
                                vibrator.cancel();
                            }else if(wake.equals("귀신소리")){
                                stopService(mute);
                            }
                            CCount = 0;
                            if (sensorManager != null)
                                sensorManager.unregisterListener(sensorEventListener);
                        }
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

    }

    //여기서부터 임시로 만든 부분. 추후 프로그램 제작시 지울것
    //옵션값이 저장됨을 확인하기 위해 일시적으로 만든 디버그용 텍스트 설정
    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences opt = getSharedPreferences("Option", MODE_PRIVATE);
        wake = opt.getString("wake","진동"); // 졸음감지 이후 깨우는 방법, 디폴트는 진동
        shut = opt.getString("shut", "흔들기"); // 알람 끄는법, 디폴트는 소리지르기
        TextView text = (TextView)findViewById(R.id.wakeShow);
        if(text!=null){
            text.setText("깨우는 방법 : " + wake);
        }
        text = (TextView)findViewById(R.id.shutShow);
        if(text!=null){
            text.setText("끄는 방법 : " + shut);
        }

    }


    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() { // 카메라 소스 창조. 위에서 퍼미션 검사 함.

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setProminentFaceOnly(true)
                .build(); // 얼굴 detector

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");
        } // 작동안하면 로그 띄워줌.

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(320,240)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setAutoFocusEnabled(true)
                .setRequestedFps(0.1f)
                .build(); // 카메라 소스 창조.
        startNotification();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(mCameraSource != null) {
            mCameraSource.stop();
        }
        isSetting = false;
        startCameraSource();
        isForeGround = true;
        if(notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
        try {
            startNotification();
            try {
                if (isSetting == false)
                {
                    mCameraSource.start();
                }

            }catch (SecurityException e){
                e.printStackTrace();
            }
            isForeGround = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
        stopService(mute); // 혹시 켜져 있으면 프로그램 죽을 경우 서비스 멈춰줌.
        // 알림창 종료
        notificationManager.cancelAll();
        vibrator.cancel();
    }


    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() { // 카메라 시작

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    public void training(View view) {
        isTrain = true;
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
        //이게 아마 페이스 객체 생성해서 이 정보를 오버레이에 띄워주는 건가봐
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

                GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        // 아이디 부여
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        // 여기서 대부분의 작업 실행 얼굴 정보 업데이트 될 때
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face); //바뀐 정보 업데이트

            Log.w("count", String.valueOf(CCount) + " //" + String.valueOf(OCount));
            // 눈 감고 있고 시작 시점이 아니며 눈 감고 있는 카운트가 30 이 되면 open count는 필요 없어
            // 눈감고 있는건 3초 , 눈뜬건 6초 기준으로 잡음.
            if (!isTrain){
                if(face.getIsLeftEyeOpenProbability() == -1.00 || face.getIsRightEyeOpenProbability() == -1.00) {

                }
                else if (face.getIsRightEyeOpenProbability() < rightClosed && face.getIsLeftEyeOpenProbability() < leftClosed && isMute == 1 && CCount == 30) {
                    CCount = CCount + 1; // 얘를 1 상승 시켜서 31으로 만듦. 0으로 만들경우 밑의 경우에 걸려
                    OCount = 0; // open카운트는 0으로 만들어버림.
                    if(wake.equals("음악") || wake.equals("귀신소리")){
                        startService(FaceTrackerActivity.mute);
//                        if(!shut.equals("패턴인식")){
//                            Intent ghostIntent = new Intent("com.google.android.gms.samples.vision.face.facetracker.ghost");
//                            ghostIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);   // 이거 안해주면 안됨
//                            startActivity(ghostIntent);
//                        }


                        //dialogDecibel();
                    }else if(wake.equals("진동")){
                        vibrator.vibrate(pattern,0);
                    }

                    if(shut.equals("소리지르기")){
                        isRecorded = true;
                        isThreadRun = true; // 녹음 쓰레드를 시작시키게 함.
                        startListenAudio(); // 오디오 녹음과 데시벨 측정 시작.
                    }else if(shut.equals("흔들기")){
                        if (accelerormeterSensor != null)
                            sensorManager.registerListener(sensorEventListener,accelerormeterSensor,SensorManager.SENSOR_DELAY_GAME);
                    }else if(shut.equals("패턴인식")){

                        Intent intent = new Intent("com.google.android.gms.samples.vision.face.facetracker.pattern");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);   // 이거 안해주면 안됨
                        startActivity(intent);

                    }

                }
                //눈감는 상황에는 오픈 카운트 아예 측정 안함.
                else if (face.getIsRightEyeOpenProbability() < rightClosed && face.getIsLeftEyeOpenProbability() < leftClosed && isMute == 1 && CCount < 30) {
                    CCount = CCount + 1;
                }
                // 눈 뜨고 있을 경우 OCount를 1 올려줌.
                else if (face.getIsRightEyeOpenProbability() > rightClosed && face.getIsLeftEyeOpenProbability() > leftClosed && isMute == 1 && CCount < 30 && OCount < 50) {
                    OCount = OCount + 1;
                    tRightClosed = tRightClosed + face.getIsRightEyeOpenProbability();
                    tLeftClosed = tLeftClosed + face.getIsLeftEyeOpenProbability();
                }
                // 눈뜨는 카운터가 60이상이 될 경우 눈 감는 카운터 역시 0으로 만들어줌.
                else if (face.getIsRightEyeOpenProbability() > rightClosed && face.getIsLeftEyeOpenProbability() > leftClosed && isMute == 1 && CCount < 30 && OCount >= 50) {
//                    if(tRightClosed/OCount - 0.3f<0.75 && tLeftClosed/OCount - 0.3f < 0.75){
//                        rightClosed = tRightClosed/OCount - 0.2f;
//                        leftClosed = tLeftClosed/OCount - 0.2f;
//                    }
                    OCount = 0;
                    CCount = 0;
                    tRightClosed = 0;
                    tLeftClosed = 0;
                    Log.d("Counts","right: " + String.valueOf(rightClosed));
                    Log.d("Counts","left : " + String.valueOf(leftClosed));

                }
                // 처음 프로그램 시작시 혹시 켜져 있을 서비스를 꺼주고 mute를 1로 만들어줌.
                else if (isMute == 0) {
                    stopService(mute);
                    isMute = 1;
                    if(mRecorder!=null){
                        mRecorder.stop();
                    }
                } else {

                }
            }else if(isTrain){
                if(TCount<60) {
                    if(face.getIsLeftEyeOpenProbability() == -1.00 || face.getIsRightEyeOpenProbability() == -1.00) {

                    }else{
                        if(TCount == 0) {rightClosed = 0; leftClosed = 0;}
                        rightClosed = rightClosed + face.getIsRightEyeOpenProbability();
                        leftClosed = leftClosed + face.getIsLeftEyeOpenProbability();
                        Log.d("Counts","right: " + String.valueOf(rightClosed));
                        Log.d("Counts","left : " + String.valueOf(leftClosed));
                        TCount = TCount + 1;
                        if(TCount == 58){
                            vibrator.vibrate(200);
                        }
                    }
                } else if(TCount>=60) {
                    Log.d("Counts","right: " + String.valueOf(rightClosed / TCount - 0.2));
                    Log.d("Counts","left : " + String.valueOf(leftClosed/TCount - 0.2));
                    rightClosed = rightClosed / TCount - 0.2f;
                    leftClosed = leftClosed / TCount - 0.2f;
                    if(rightClosed < 0.7 && leftClosed < 0.7){

                        Log.d("Counts","right: " + String.valueOf(rightClosed));
                        Log.d("Counts","left : " + String.valueOf(leftClosed));

                        isTrain = false;
                        TCount = 0;
                        OCount = 0;
                        CCount = 0;

                    }else{
                        rightClosed = 0.5f;
                        leftClosed = 0.5f;

                        TCount = 0;

                        isTrain = false;
                        OCount = 0;
                        CCount = 0;
                    }

                }
                else{
                    rightClosed = 0.5f;
                    leftClosed = 0.5f;
                    isTrain = false;
                    TCount = 0;
                }
            }

        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
            //stopService(mute);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }

    public void startOption(View view){
        if(CCount >30){
            Toast.makeText(activity, "알람을 먼저 끈 후에 시도해주세요", Toast.LENGTH_SHORT).show();
        }else{
            isSetting = true;
            Intent intent1 = new Intent(this, TabActivity.class);
            startActivity(intent1);
        }
    }

    // 데시벨 관련 소스 시작
    // 이 핸들러는 데시벨 측정시 얘가 측정값을 분석함.
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Log.w("decibel", String.valueOf(World.dbCount));
                if (World.dbCount > 90) { // 데시벨이 80이상이면
                    //  ad.cancel();
                    CCount = 0;
                    OCount = 0;
                    if(wake.equals("음악")) {
                        isRecorded = false;
                        stopService(mute); //꺼줌.
                    }else if(wake.equals("진동")){
                        isRecorded = false;
                        vibrator.cancel();
                    }else if(wake.equals("귀신소리")){
                        isRecorded = false;
                        stopService(mute);
                    }


                    if (thread != null) { // 스레드가 널이 아니면
                        isThreadRun = false; // false 주고 스레드 죽임.
                        thread = null;
                    }
                }
            }
        }
    };


    // 오디오 시작.
    private void startListenAudio() {
        // 스레드 생성
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mRecorder.start(); // 녹음 시작.
                while (isThreadRun) { // 돌면
                    if (bListener) { // 리스너 있으면
                        volume = mRecorder.getAmplitude();  // 녹음기로 부터 소리 받아옴.
                        if (volume > 0 && volume < 1000000) { // 최대 최소 크기 설정.
                            World.setDbCount(20 * (float) (Math.log10(volume)));  // 데시벨 크기로 볼륨을 바꿔서 World에 넘김.
                            Message message = new Message();
                            message.what = 1; // 일단 이렇게 해둔게 예제에서 이런식으로 해놨음.
                            handler.sendMessage(message); // 이렇게 해서 핸들러 작동 시킴.
                        }
                    }
                }
                if (!isThreadRun) {
                    mRecorder.stop();
                }
            }
        });
        thread.start();
    }

    // onCreate에서 불려져서 알림창을 생성함
    private void startNotification(){
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        notification = new Notification(R.drawable.icon, null,System.currentTimeMillis());

        notificationView = new RemoteViews(getPackageName(),R.layout.notification);

        ButtonListener buttonListener = new ButtonListener();

        // 알림창을 눌렀을때 (버튼이 아닌 외부) FaceTracker 실행
        Intent notificationIntent = new Intent(this, FaceTrackerActivity.class);
        PendingIntent mPendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);

        // ▷ 버튼을 눌렀을 때
        Intent startIntent = new Intent(this, buttonListener.getClass());
        startIntent.putExtra("key","start");
        PendingIntent pStartIntent = PendingIntent.getBroadcast(this,1,startIntent,0);

        // || 버튼 눌렀을 때
        Intent pauseIntent = new Intent(this, buttonListener.getClass());
        pauseIntent.putExtra("key","pause");
        PendingIntent pPauseIntent = PendingIntent.getBroadcast(this,2,pauseIntent,0);

        // 설정 버튼 눌렀을 때
        Intent settingIntent = new Intent(this, TabActivity.class);
        PendingIntent pSettingIntent = PendingIntent.getActivity(this,3,settingIntent,0);

        // X 버튼 눌렀을 때
        Intent closeIntent = new Intent(this, buttonListener.getClass());
        closeIntent.putExtra("key","close");
        PendingIntent pCloseIntent = PendingIntent.getBroadcast(this,4,closeIntent,0);

        // 알림 설정과 intent 부여
        notification.contentView = notificationView;
        notification.contentIntent = mPendingIntent;
        notification.flags |= Notification.FLAG_NO_CLEAR;

        // 버튼이 눌릴때 불려질 PendingIntent 설정
        notificationView.setOnClickPendingIntent(R.id.start, pStartIntent);
        notificationView.setOnClickPendingIntent(R.id.pause, pPauseIntent);
        notificationView.setOnClickPendingIntent(R.id.setting, pSettingIntent);
        notificationView.setOnClickPendingIntent(R.id.close, pCloseIntent);


        // 알림 실행
        notificationManager.notify(1,notification);
    }

    // shake 센서 변동시 불리는 메소드
    @Override
    public void onSensorChanged(SensorEvent event) { }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    // manifests에 명시할때 주의할 것
    public static class ButtonListener extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String st = intent.getStringExtra("key");

            switch (st) {
                case "start":
                    Toast.makeText(context, "얼굴 인식 시작", Toast.LENGTH_SHORT).show();
                    mPreview.stop();
                    try {
                        try {
                            mCameraSource.start();
                        }catch (SecurityException e){
                            e.printStackTrace();
                        }
                        if(isRecorded == true){
                            // startService(mute);
                            mRecorder.start();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "pause":
                    Toast.makeText(context, "얼굴 인식 정지", Toast.LENGTH_SHORT).show();
                    if(isRecorded == false){
                        mCameraSource.stop();
                    }else if(isRecorded == true){
                        Toast.makeText(context, "알람을 먼저 멈춰 주세요", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "setting":
                    Toast.makeText(context,"설정창으로 이동합니다.",Toast.LENGTH_SHORT).show();
                    break;
                case "close":
                    Toast.makeText(context, "얼굴인식을 종료합니다.", Toast.LENGTH_SHORT).show();
                    activity.finish();
                    break;
                default:
                    break;
            }

        }
    }
}


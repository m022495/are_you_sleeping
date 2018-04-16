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
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

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
public final class FaceTrackerActivity extends AppCompatActivity {
    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;

    public static CameraSourcePreview mPreview; // 카메라 프리 뷰
    public static  GraphicOverlay mGraphicOverlay; // 프리뷰 위에 그래픽 오버레이를 띄우나봄

    private Intent camera;

    // 알람을 위한 것들. --> update부분에서는 계속 갱신이 되는 문제 때문에 그냥 전역? 비슷하게 선언.
    public static Intent mute;
    private SharedPreferences.Editor editor;
    private int isMute; // 처음 시작할 때 음악이 켜져있는 불상사가 있을 경우 바로 끔

    // 데시벨을 위한 것들
    private MediaRecorder mRecorder; // 목소리 녹음
    private AlertDialog ad; // 다이어 그램인데 일단 아직 안씀.
    private String curVal; // 현재 볼륨
    private Thread thread; // 데시벨을 듣기위해 쓰레드를 돌림.
    float volume = 10000; // 현재 볼륨을 받기 위해 쓰레드에서 쓰는 것
    private int CCount; // 눈 깜은 거 측정하는 카운트
    private int OCount; // 눈 뜬거 측정하는 카운트

    private boolean bListener = true;
    private boolean isThreadRun = true;
    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_AUDIO_PERM = 3;
    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        // mPreview.setSystemUiVisibility();
        //mPreview.setVisibility(View.GONE);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int rc1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (rc == PackageManager.PERMISSION_GRANTED && rc1 == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
        mute = new Intent(this, AlarmService.class);

        SharedPreferences opt = getSharedPreferences("Option", MODE_PRIVATE); // 이전거 가져오나봐 자세한건 잘 모르겠다.
        editor = opt.edit(); // 얘로 처음 시작할 때 isMUte 0으로 설정
        isMute = opt.getInt("isMute", 0); // 0으로 시작해서 혹시모를 서비스를 끌 수 있도록 함.
        mRecorder = new MediaRecorder();
        CCount = 0; // 눈을 감을 때 쓸 카운트
        OCount = 0; // 눈을 뜬 시간을 가지고 올 때 쓰는 카운트
    }


    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() { // 귀찮아서 이름은 안바꿨는데 이게 permission 종합적으로 받아옴.
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        // 오디오랑 카메라 퍼미션 받자
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                int result = PermissionChecker.checkCallingOrSelfPermission(this, permission);
                if (result == PermissionChecker.PERMISSION_GRANTED) ;
                else {
                    ActivityCompat.requestPermissions(this, permissions, 1);
                }
            }
        }
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            return;
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
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setAutoFocusEnabled(true)
                .setRequestedFps(15.0f)
                .build(); // 카메라 소스 창조.

    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
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
    }

    @Override // 이것도 퍼미션
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
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
            camera = new Intent(this, CameraService.class);
            startService(camera);
//            try {
//
//                mPreview.start(mCameraSource, mGraphicOverlay);
//
//            } catch (IOException e) {
//                Log.e(TAG, "Unable to start camera source.", e);
//                mCameraSource.release();
//                mCameraSource = null;
//            }
        }
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
            if (face.getIsRightEyeOpenProbability() < 0.5 && face.getIsRightEyeOpenProbability() < 0.5 && isMute == 1 && CCount == 30) {
                CCount = CCount + 1; // 얘를 1 상승 시켜서 31으로 만듦. 0으로 만들경우 밑의 경우에 걸려
                OCount = 0; // open카운트는 0으로 만들어버림.
                startService(mute); // 알람 시작
                isThreadRun = true; // 쓰레드를 시작시키게 함.
                //dialogDecibel();
                startListenAudio(); // 오디오 녹음과 데시벨 측정 시작.
            }
            //눈감는 상황에는 오픈 카운트 아예 측정 안함.
            else if (face.getIsRightEyeOpenProbability() < 0.5 && face.getIsRightEyeOpenProbability() < 0.5 && isMute == 1 && CCount < 30) {
                CCount = CCount + 1;
            }
            // 눈 뜨고 있을 경우 OCount를 1 올려줌.
            else if (face.getIsRightEyeOpenProbability() > 0.5 && face.getIsRightEyeOpenProbability() > 0.5 && isMute == 1 && CCount < 30 && OCount < 60) {
                OCount = OCount + 1;
            }
            // 눈뜨는 카운터가 60이상이 될 경우 눈 감는 카운터 역시 0으로 만들어줌.
            else if (face.getIsRightEyeOpenProbability() > 0.5 && face.getIsRightEyeOpenProbability() > 0.5 && isMute == 1 && CCount < 30 && OCount >= 60) {
                OCount = 0;
                CCount = 0;
            }
            // 처음 프로그램 시작시 혹시 켜져 있을 서비스를 꺼주고 mute를 1로 만들어줌.
            else if (isMute == 0) {
                stopService(mute);
                isMute = 1;
            } else {

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

    // 데시벨 관련 소스 시작
    // 이 핸들러는 데시벨 측정시 얘가 측정값을 분석함.
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Log.w("decibel", String.valueOf(World.dbCount));
                if (World.dbCount > 90) { // 데시벨이 90이상이면
                    //  ad.cancel();
                    CCount = 0;
                    OCount = 0;
                    stopService(mute); //꺼줌.
                    if (thread != null) { // 스레드가 널이 아니면
                        isThreadRun = false; // false 주고 스레드 죽임.
                        thread = null;
                    }
                }
                // curVal = String.valueOf(World.dbCount); // 얘는 다이어로그 용으로 넣어준거
                //  ad.setMessage(curVal);
                //  ad.show();
            }
        }
    };

    //다이어로그 일단 놔둠.
    public void dialogDecibel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getApplicationContext());
        ad = builder.create();
        ad.setTitle("60데시벨이상 고함치세요");
        ad.setMessage("데시벨~");
        ad.show();
    }

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
}
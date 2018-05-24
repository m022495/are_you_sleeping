package com.google.android.gms.samples.vision.face.facetracker;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by cosmos on 2018-05-11.
 */

public class LoadingActivity extends AppCompatActivity {

    private static final String TAG = "FaceTracker";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity);

        checkPermission();
    }

    private void checkPermission(){
        // 마쉬멜로우 이상인지 체크
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // 카메라 권한 유무 판단
            int cameraPermissionResult = checkSelfPermission(Manifest.permission.CAMERA);

            if( cameraPermissionResult == PackageManager.PERMISSION_DENIED){
                if( shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(LoadingActivity.this);
                    dialog.setTitle("권한이 필요합니다.")
                            .setMessage("이 어플리케이션은 카메라 권한이 필수입니다. 계속 하시겠습니까?")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.CAMERA}, 1000);
                                    }
                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(LoadingActivity.this, "기능을 취소했습니다.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                            .create()
                            .show();
                }
                else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA},1000);

                }
            }
            else{
                startActivity(new Intent(LoadingActivity.this, FaceTrackerActivity.class));
                finish();
            }
        }
        else{
            startActivity(new Intent(LoadingActivity.this, FaceTrackerActivity.class));
            finish();
        }

    }


    // 권한 요청에 대한 응답 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 1000){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        ==PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(LoadingActivity.this, FaceTrackerActivity.class));
                    finish();
                }
            }
            else {
                Toast.makeText(LoadingActivity.this,"권한요청을 거부했습니다.",Toast.LENGTH_SHORT).show();
                AlertDialog.Builder dialog = new AlertDialog.Builder(LoadingActivity.this);
                dialog.setTitle("권한이 필요합니다.")
                        .setMessage("이 어플리케이션은 카메라 권한이 필수입니다. 권한을 설정하지 않으면 앱을 동작할 수 없습니다. 권한을 재요청 하시겠습니까?")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 1000);
                                }
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(LoadingActivity.this, "앱을 종료합니다.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .create()
                        .show();
            }
        }
    }
}

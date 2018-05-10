package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class OptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
    }
    public void wakePopup(View v){
        //알람 설정을 위한 팝업 띄우기
        Intent intent = new Intent(this, PopupActivity.class);
        intent.putExtra("option", "wake");
        startActivityForResult(intent, 1);
    }
    public void shutPopup(View v){
        //끄기 설정을 위한 팝업 띄우기
        Intent intent = new Intent(this, PopupActivity.class);
        intent.putExtra("option", "shut");
        startActivityForResult(intent, 1);
    }
    public void faceDetecting(View v){
        //얼굴 인식 설정을 위한 팝업 띄우기
    }
}

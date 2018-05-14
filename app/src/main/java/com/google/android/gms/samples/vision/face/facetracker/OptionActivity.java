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
        SharedPreferences opt = getSharedPreferences("Option", MODE_PRIVATE);
        String wake = opt.getString("wake","설정되지 않음"); // 졸음감지 이후 깨우는 방법, 디폴트는 진동
        String shut = opt.getString("shut", "설정되지 않음"); // 알람 끄는법, 디폴트는 소리지르기
        TextView text = (TextView)findViewById(R.id.wakeShow);
        text.setText(wake);
        text = (TextView)findViewById(R.id.shutShow);
        text.setText(shut);
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

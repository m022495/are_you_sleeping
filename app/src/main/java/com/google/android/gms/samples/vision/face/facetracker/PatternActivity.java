package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.camera2.params.Face;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.Toast;

import com.eftimoff.patternview.PatternView;

public class PatternActivity extends AppCompatActivity {
    private PatternView patternView;
    private String wake;
    private String patternString;
    private String data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pattern);
        //타이틀바 없애기
        Intent intent = getIntent();

        data = intent.getStringExtra("option");


        patternView = (PatternView) findViewById(R.id.patternView);
        patternView.setTactileFeedbackEnabled(false);
        Toast.makeText(getApplicationContext(), "ENTER PATTERN", Toast.LENGTH_LONG).show();
        patternView.setPathColor(Color.BLACK);
        patternView.setDotColor(Color.BLACK);
        patternView.setCircleColor(Color.BLACK);


        patternView.setOnPatternDetectedListener(new PatternView.OnPatternDetectedListener() {

            @Override
            public void onPatternDetected() {
                SharedPreferences opt = getSharedPreferences("Option", MODE_PRIVATE); // 이전거 가져오나봐 자세한건 잘 모르겠다
                String patternT = opt.getString("patternT","0"); // 졸음감지 이후 깨우는 방법, 디폴트는 진동

                patternString = opt.getString("pattern",null);
                if (patternString == null || patternT.equals("0")) {
                    SharedPreferences.Editor editor = opt.edit();
                    Toast.makeText(PatternActivity.this, "사용하실 패턴을 입력하세요.", Toast.LENGTH_SHORT).show();
                    patternString = patternView.getPatternString();
                    editor.putString("pattern",patternString);
                    editor.putString("patternT","1");
                    patternView.clearPattern();
                    editor.commit();
                    finish();
                    return;
                }
                if (patternString.equals(patternView.getPatternString())) {
                    Toast.makeText(getApplicationContext(), "PATTERN CORRECT", Toast.LENGTH_SHORT).show();
                    wake = opt.getString("wake","진동");
                    if(wake.equals("진동")){
                        FaceTrackerActivity.vibrator.cancel();
                    }else if(wake.equals("음악")){
                        FaceTrackerActivity.isMute = 1;
                        stopService(FaceTrackerActivity.mute);
                    }
                    FaceTrackerActivity.CCount = 0;
                    finish();
                    return;
                }else{
                    Toast.makeText(getApplicationContext(), "PATTERN NOT CORRECT", Toast.LENGTH_SHORT).show();
                    patternView.clearPattern();
                }
            }
        });
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }


}

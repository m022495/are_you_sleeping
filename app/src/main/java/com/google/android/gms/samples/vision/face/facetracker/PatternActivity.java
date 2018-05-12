package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
        SharedPreferences opt = getSharedPreferences("Option", MODE_PRIVATE); // 이전거 가져오나봐 자세한건 잘 모르겠다.

        wake = opt.getString("wake","진동"); // 졸음감지 이후 깨우는 방법, 디폴트는 진동
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
                if (patternString == null) {
                    patternString = patternView.getPatternString();
                    patternView.clearPattern();
                    return;
                }
                if (patternString.equals(patternView.getPatternString())) {
                    Toast.makeText(getApplicationContext(), "PATTERN CORRECT", Toast.LENGTH_SHORT).show();
                    patternView.clearPattern();
                    return;
                }
                Toast.makeText(getApplicationContext(), "PATTERN NOT CORRECT", Toast.LENGTH_SHORT).show();
                patternView.clearPattern();
            }
        });
    }
}

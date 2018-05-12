package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class PopupActivity extends AppCompatActivity {
    private String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup);
        Intent intent = getIntent();
        data = intent.getStringExtra("option");

        Button opt1 = (Button)findViewById(R.id.select1);
        Button opt2 = (Button)findViewById(R.id.select2);
        Button opt3 = (Button)findViewById(R.id.select3);
        Button opt4 = (Button)findViewById(R.id.select4);

        switch (data){
            case "wake":
                opt1.setText("진동");
                opt2.setText("음악");
                opt3.setText("귀신소리");
                opt4.setVisibility(View.INVISIBLE);
                break;
            case "shut":
                opt1.setText("소리지르기");
                opt2.setText("흔들기");
                opt3.setText("단순버튼");
                opt4.setText("패턴인식");
                break;
            default:
                finish();
                break;
        }
    }

    //확인 버튼 클릭
    public void clickOpt1(View v){
        //데이터 전달하기
        SharedPreferences opt = getSharedPreferences("Option",MODE_PRIVATE);
        SharedPreferences.Editor editor = opt.edit();

        Button name = (Button)findViewById(R.id.select1);
        switch (data){
            case "wake":
               editor.putString("wake","진동");
                break;
            case "shut":
                editor.putString("shut","소리지르기");
                break;
            default:
                finish();
                break;
        }

        editor.commit();
        //액티비티(팝업) 닫기
        finish();
    }
    public void clickOpt2(View v){
        //데이터 전달하기
        SharedPreferences opt = getSharedPreferences("Option",MODE_PRIVATE);
        SharedPreferences.Editor editor = opt.edit();

        Button name = (Button)findViewById(R.id.select1);
        switch (data){
            case "wake":
                editor.putString("wake","음악");
                break;
            case "shut":
                editor.putString("shut","흔들기");
                break;
            default:
                finish();
                break;
        }

        editor.commit();
        //액티비티(팝업) 닫기
        finish();
    }
    public void clickOpt3(View v){
        //데이터 전달하기
        SharedPreferences opt = getSharedPreferences("Option",MODE_PRIVATE);
        SharedPreferences.Editor editor = opt.edit();

        Button name = (Button)findViewById(R.id.select1);
        switch (data){
            case "wake":
                editor.putString("wake","귀신소리");
                break;
            case "shut":
                editor.putString("shut","단순버튼");
                break;
            default:
                finish();
                break;
        }

        editor.commit();
        //액티비티(팝업) 닫기
        finish();
    }
    public void clickOpt4(View v) {
        //데이터 전달하기
        SharedPreferences opt = getSharedPreferences("Option", MODE_PRIVATE);
        SharedPreferences.Editor editor = opt.edit();

        Button name = (Button) findViewById(R.id.select1);
        switch (data){
            case "shut":
                editor.putString("shut", "패턴인식");
                String a  = opt.getString("pattern",null);
                if(a == null){
                    Intent intent = new Intent(this, PatternActivity.class);
                    intent.putExtra("option", "wake");
                    startActivityForResult(intent, 1);
                }
                break;
            default:
                finish();
                break;
        }

        editor.commit();
        //액티비티(팝업) 닫기
        finish();
    }
}
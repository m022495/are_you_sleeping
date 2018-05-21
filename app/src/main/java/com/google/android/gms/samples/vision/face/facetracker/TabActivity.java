package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ToggleButton;

public class TabActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);


        TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec spec1=tabHost.newTabSpec("Tab 1");
        spec1.setContent(R.id.tab1);
        spec1.setIndicator("깨우기");
        TabHost.TabSpec spec2=tabHost.newTabSpec("Tab 2");
        spec2.setIndicator("끄기");
        spec2.setContent(R.id.tab2);
        TabHost.TabSpec spec3=tabHost.newTabSpec("Tab 3");
        spec3.setIndicator("기타");
        spec3.setContent(R.id.tab3);
        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
        refresh();
    }

    public void clickWB1(View v){
        SharedPreferences opt = getSharedPreferences("Option",MODE_PRIVATE);
        SharedPreferences.Editor editor = opt.edit();
        editor.putString("wake","음악");
        editor.commit();
        refresh();
    }
    public void clickWB2(View v){
        SharedPreferences opt = getSharedPreferences("Option",MODE_PRIVATE);
        SharedPreferences.Editor editor = opt.edit();
        editor.putString("wake","진동");
        editor.commit();
        refresh();
    }
    public void clickWB3(View v){
        SharedPreferences opt = getSharedPreferences("Option",MODE_PRIVATE);
        SharedPreferences.Editor editor = opt.edit();
        editor.putString("wake","귀신소리");
        editor.commit();
        refresh();
    }

    public void clickSB1(View v){
        SharedPreferences opt = getSharedPreferences("Option",MODE_PRIVATE);
        SharedPreferences.Editor editor = opt.edit();
        editor.putString("shut","소리지르기");
        editor.commit();
        refresh();
    }
    public void clickSB2(View v){
        SharedPreferences opt = getSharedPreferences("Option",MODE_PRIVATE);
        SharedPreferences.Editor editor = opt.edit();
        editor.putString("shut","흔들기");
        editor.commit();
        refresh();
    }
    public void clickSB3(View v){
        SharedPreferences opt = getSharedPreferences("Option",MODE_PRIVATE);
        SharedPreferences.Editor editor = opt.edit();
        editor.putString("shut","단순버튼");
        editor.commit();
        refresh();
    }
    public void clickSB4(View v){
        SharedPreferences opt = getSharedPreferences("Option",MODE_PRIVATE);
        SharedPreferences.Editor editor = opt.edit();
        editor.putString("shut","패턴인식");
        editor.commit();
        SharedPreferences opt1 = getSharedPreferences("Option", MODE_PRIVATE); // 이전거 가져오나봐 자세한건 잘 모르겠다.
        String pattern1 = opt1.getString("patternT","0"); // 졸음감지 이후 깨우는 방법, 디폴트는 진동
        if(pattern1.equals("0")){
            Intent intent = new Intent("com.google.android.gms.samples.vision.face.facetracker.pattern");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);   // 이거 안해주면 안됨
            startActivity(intent);
        }
        refresh();
    }

    public void refresh(){
        SharedPreferences opt = getSharedPreferences("Option", MODE_PRIVATE);

        ToggleButton tg = (ToggleButton)findViewById(R.id.wakeButton1);
        LinearLayout tab = (LinearLayout)findViewById(R.id.wakeTab1);
        tg.setChecked(false);
        tab.setVisibility(View.GONE);
        tg = (ToggleButton)findViewById(R.id.wakeButton2);
        tab = (LinearLayout)findViewById(R.id.wakeTab2);
        tg.setChecked(false);
        tab.setVisibility(View.GONE);
        tg = (ToggleButton)findViewById(R.id.wakeButton3);
        tab = (LinearLayout)findViewById(R.id.wakeTab3);
        tg.setChecked(false);
        tab.setVisibility(View.GONE);
        tg = (ToggleButton)findViewById(R.id.shutButton1);
        tab = (LinearLayout)findViewById(R.id.shutTab1);
        tg.setChecked(false);
        tab.setVisibility(View.GONE);
        tg = (ToggleButton)findViewById(R.id.shutButton2);
        tab = (LinearLayout)findViewById(R.id.shutTab2);
        tg.setChecked(false);
        tab.setVisibility(View.GONE);
        tg = (ToggleButton)findViewById(R.id.shutButton3);
        tab = (LinearLayout)findViewById(R.id.shutTab3);
        tg.setChecked(false);
        tab.setVisibility(View.GONE);
        tg = (ToggleButton)findViewById(R.id.shutButton4);
        tab = (LinearLayout)findViewById(R.id.shutTab4);
        tg.setChecked(false);
        tab.setVisibility(View.GONE);

        switch (opt.getString("wake","음악")) {
            case "음악":
                tg = (ToggleButton) findViewById(R.id.wakeButton1);
                tab = (LinearLayout) findViewById(R.id.wakeTab1);
                tg.setChecked(true);
                tab.setVisibility(View.VISIBLE);
                break;
            case "진동":
                tg = (ToggleButton) findViewById(R.id.wakeButton2);
                tab = (LinearLayout) findViewById(R.id.wakeTab2);
                tg.setChecked(true);
                tab.setVisibility(View.VISIBLE);
                break;
            case "귀신소리":
                tg = (ToggleButton) findViewById(R.id.wakeButton3);
                tab = (LinearLayout) findViewById(R.id.wakeTab3);
                tg.setChecked(true);
                tab.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
        switch (opt.getString("shut","소리지르기")) {
            case "소리지르기":
                tg = (ToggleButton) findViewById(R.id.shutButton1);
                tab = (LinearLayout) findViewById(R.id.shutTab1);
                tg.setChecked(true);
                tab.setVisibility(View.VISIBLE);
                break;
            case "흔들기":
                tg = (ToggleButton) findViewById(R.id.shutButton2);
                tab = (LinearLayout) findViewById(R.id.shutTab2);
                tg.setChecked(true);
                tab.setVisibility(View.VISIBLE);
                break;
            case "단순버튼":
                tg = (ToggleButton) findViewById(R.id.shutButton3);
                tab = (LinearLayout) findViewById(R.id.shutTab3);
                tg.setChecked(true);
                tab.setVisibility(View.VISIBLE);
                break;
            case "패턴인식":
                tg = (ToggleButton) findViewById(R.id.shutButton4);
                tab = (LinearLayout) findViewById(R.id.shutTab4);
                tg.setChecked(true);
                tab.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }

    }
}

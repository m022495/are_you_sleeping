package com.google.android.gms.samples.vision.face.facetracker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class TabActivity extends AppCompatActivity {

    private static final int RC_HANDLE_AUDIO_PERM = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec spec1=tabHost.newTabSpec("Tab 1");
        spec1.setIndicator("깨우기");
        spec1.setContent(R.id.tab1);
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
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (rc != PackageManager.PERMISSION_GRANTED) {
            requestAudioPermission();
        } else {
            SharedPreferences opt = getSharedPreferences("Option", MODE_PRIVATE);
            SharedPreferences.Editor editor = opt.edit();
            editor.putString("shut", "소리지르기");
            editor.commit();
            refresh();
        }
    }
    public void clickSB2(View v){
        SharedPreferences opt = getSharedPreferences("Option",MODE_PRIVATE);
        SharedPreferences.Editor editor = opt.edit();
        editor.putString("shut","흔들기");
        editor.commit();
        refresh();
    }

    public void clickSB4(View v){
        SharedPreferences opt = getSharedPreferences("Option",MODE_PRIVATE);
        SharedPreferences.Editor editor = opt.edit();
        editor.putString("shut","패턴인식");
        editor.commit();
        SharedPreferences opt1 = getSharedPreferences("Option", MODE_PRIVATE); // 이전거 가져오나봐 자세한건 잘 모르겠다.
        String pattern1 = opt1.getString("patternT","0");
        if(pattern1.equals("0")){
            Intent intent = new Intent("com.google.android.gms.samples.vision.face.facetracker.pattern");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);   // 이거 안해주면 안됨
            startActivity(intent);
        }
        refresh();
    }

    public void clickDev(View v){
        AlertDialog.Builder ad = new AlertDialog.Builder(TabActivity.this);
        ad.setTitle("개발자");
        TextView tv = new TextView(TabActivity.this);
        tv.setText("이 앱은 명지대학교 컴퓨터공학과 캡스톤디자인1을 수강하는 학생들에 의해 만들어졌습니다.");
        ad.setView(tv);
        ad.show();
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

    // 기존 두개 퍼미션 받는거에서 카메라는 로딩에서 받았고, Audio만 받게 변경
    private void requestAudioPermission() {

        // 버전에 맞춰서 오디오 퍼미션 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int audioPermissionResult = checkSelfPermission(Manifest.permission.RECORD_AUDIO);

            if( audioPermissionResult == PackageManager.PERMISSION_DENIED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(TabActivity.this);
                    dialog.setTitle("권한이 필요합니다.")
                            .setMessage("해당 기능은 마이크 권한을 필요로 합니다. 권한이 없을시 기능이 동작하지 않습니다. 권한을 요청하시겠습니까?")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, RC_HANDLE_AUDIO_PERM);
                                    }
                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(TabActivity.this, "녹음 기능을 사용하지 않습니다.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .create()
                            .show();
                }
                else {
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, RC_HANDLE_AUDIO_PERM);
                }
            }
        }

    }

    @Override // 퍼미션을 요청했을 경우 여기서 반환값을 만들어 내줌.
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == RC_HANDLE_AUDIO_PERM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(TabActivity.this, "녹음 권한을 승인했습니다.", Toast.LENGTH_SHORT).show();
                    SharedPreferences opt = getSharedPreferences("Option", MODE_PRIVATE);
                    SharedPreferences.Editor editor = opt.edit();
                    editor.putString("shut", "소리지르기");
                    editor.commit();
                    refresh();
                }
            } else {
                Toast.makeText(TabActivity.this, "녹음 권한을 거부했습니다. 해당 기능은 녹음권한이 없을 시 사용이 불가능합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

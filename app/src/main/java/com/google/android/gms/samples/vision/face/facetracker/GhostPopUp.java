package com.google.android.gms.samples.vision.face.facetracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class GhostPopUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghost_pop_up);


    }

    public void finishGost(View view) {
        finish();
    }
}

package com.google.android.gms.samples.vision.face.facetracker;

/**
 * Created by vkdlv on 2018-04-09.
 */

// 원래 레코더가 넘기는 볼륨 값이 데시벨 값이 아니야. 얘는 데시벨 값으로 바꿔주는 애
public class World {
    // 데시벨 바꿔주는 것? 일단 있는거 복붙
    public static float dbCount = 40;
    public static float minDB =100;
    public static float maxDB =0;
    public static float lastDbCount = dbCount;
    private static float min = 0.5f;  //Set the minimum sound change
    private static float value = 0;   // Sound decibel value

    // 받은 데시벨 값으로 조작해줌.  최대 최소 데시벨 해놓은거.
    // 뭔가 필요없는거 같은데 일단이걸로 놔둠 혹시 필요할 때 있을지 몰라!
    public static void setDbCount(float dbValue) {
        if (dbValue > lastDbCount) {
            value = dbValue - lastDbCount > min ? dbValue - lastDbCount : min;
        }else{
            value = dbValue - lastDbCount < -min ? dbValue - lastDbCount : -min;
        }
        dbCount = lastDbCount + value * 0.2f ; //To prevent the sound from changing too fast
        lastDbCount = dbCount;
        if(dbCount<minDB) minDB=dbCount;
        if(dbCount>maxDB) maxDB=dbCount;
    }
}

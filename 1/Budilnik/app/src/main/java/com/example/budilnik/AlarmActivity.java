package com.example.budilnik;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.Nullable;

public class AlarmActivity extends AppCompatActivity {

    Ringtone ringtone;
    Vibrator vibrator;
    public static Integer hour;
    public static Integer min;
    public boolean flagVkl;
    static Switch switchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (MainActivity.hour!=null&&MainActivity.min!=null){
            hour=MainActivity.hour;
            min=MainActivity.min;
        }else {
            SharedPreferences savedData = getSharedPreferences("SavedData",MODE_PRIVATE);

            hour=savedData.getInt("Hours",0);
            min=savedData.getInt("Min",0);
        }
        otmena();
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);


        setContentView(R.layout.activity_alarm);

        Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(this,notificationUri);
        if (ringtone==null){
            notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this,notificationUri);
        }
        if (ringtone!=null){
            ringtone.play();
        }
        long[] pattern = {0, 2000, 1000, 2500, 1500, 3000};
        vibrator.vibrate(pattern,1);

    }

    @Override
    protected void onStop() {
        super.onStop();
        otmena();
    }

    @Override
    protected void onDestroy() {
        SharedPreferences savedData = getSharedPreferences("SavedData",MODE_PRIVATE);//сохранение данных
        flagVkl=false;
        SharedPreferences.Editor editor=savedData.edit();//сохранение данных
        editor.putBoolean("flag",flagVkl);
        editor.apply();
        if (ringtone!=null&&ringtone.isPlaying()){
            ringtone.stop();

        }
        vibrator.cancel();
        MainActivity.hour=this.hour;
        MainActivity.min=this.min;
        otmena();
        super.onDestroy();
    }


    public void cansel(View view) {
        Intent intent = new Intent(this,MainActivity.class);
        if (ringtone!=null&&ringtone.isPlaying()){
            ringtone.stop();

        }
        vibrator.cancel();
        otmena();
        startActivity(intent);
    }

    public void otmena(){
SharedPreferences savedData = getSharedPreferences("SavedData",MODE_PRIVATE);//сохранение данных
        hour=savedData.getInt("Hours",0);
        min=savedData.getInt("Min",0);
        switchView=findViewById(R.id.switch_bn1);
        MainActivity.alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (getAlarmActionPendingIntent()!=null){
            MainActivity.alarmManager.cancel(getAlarmActionPendingIntent());}//Отменяем будильник, связанный с интентом данного класса
       // Toast.makeText(this, "Будильник отменён", Toast.LENGTH_LONG).show();
        MainActivity.flagVkl=false;
        MainActivity.switchView.setChecked(false);
        SharedPreferences.Editor editor=savedData.edit();//сохранение данных
        editor.putInt("Hours", hour);
        editor.putInt("Min", min);
        editor.putBoolean("flag",flagVkl);
        editor.apply();
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private PendingIntent getAlarmInfoPendingIntent(){
        Intent alarmInfoIntent  = new Intent(this,MainActivity.class);
        alarmInfoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this,0,alarmInfoIntent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    PendingIntent getAlarmActionPendingIntent(){//Что произойдёт когда сработает будильник
        Intent intent = new Intent(this,AlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

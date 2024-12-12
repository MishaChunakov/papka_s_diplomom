package com.example.budilnik;

import static com.example.budilnik.BluetoothLeService.heartRate;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity   {
    public static int heartRate;
    SimpleDateFormat sdf = new SimpleDateFormat("dd LLLL HH:mm:ss", Locale.getDefault());
    public static Integer hour;
    public static Integer min;
    public static Integer day;
    public static Integer hour2;
    public static Integer min2;
    public static Integer day2;
    final Context context = this;
    static AlarmManager alarmManager;
    public static final int REQUEST_CODE_LOC=1;
    static Switch switchView;
    Calendar calendar_zavtro = Calendar.getInstance();
    Calendar calendar_segodny = Calendar.getInstance();
    public static boolean flagVkl=false;
    private int interval;
    public static boolean flagVklB=false;
    public static boolean flagVklI=false;


    @Override
    protected void onDestroy() {
        flagVkl=false;
        SharedPreferences savedData = getSharedPreferences("SavedData",MODE_PRIVATE);//сохранение данных
        SharedPreferences.Editor editor=savedData.edit();//сохранение данных
        editor.putBoolean("flag",flagVkl);
        editor.apply();
        AlarmActivity.hour= hour;
        AlarmActivity.min= min;

        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        razrysheniy();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(MainActivity.this,
                        MainBluetooth.class);
                startActivity(intent);
            }
        });
        FloatingActionButton interval2 = findViewById(R.id.interval);
        interval2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
// get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.prompts, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // get user input and set it to result
                                        // edit text


                                        String im =String.valueOf(userInput.getText());
                                        try {
                                            interval=Integer.parseInt (im);
                                        }catch (NumberFormatException e){
                                            Toast.makeText(MainActivity.this, "Вы ввели неправильное число.\n(Вы могли ввести число с буквой или дробное число)", Toast.LENGTH_LONG).show();
                                        interval=20;
                                        }


                                        SharedPreferences savedData = getSharedPreferences("SavedData",MODE_PRIVATE);//сохранение данных
                                        SharedPreferences.Editor editor=savedData.edit();//сохранение данных
                                        editor.putInt("interval", interval);
                                        editor.apply();
                                        if (min<10){
                                            if (hour<10){
                                                switchView.setText("0"+hour+":0"+min+" Интервал ="+interval+" мин");
                                            }else{
                                                switchView.setText(hour+":0"+min+" Интервал ="+interval+" мин");
                                            }}else{
                                            if (hour<10){
                                                switchView.setText("0"+hour+":"+min+" Интервал ="+interval+" мин");
                                            }else{
                                                switchView.setText(hour+":"+min+" Интервал ="+interval+" мин");
                                            }
                                        }
                                    }
                                })
                        .setNegativeButton("Отмена",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });
        SharedPreferences savedData = getSharedPreferences("SavedData",MODE_PRIVATE);//сохранение данных
        interval = savedData.getInt("interval", 30);
        hour=savedData.getInt("Hours",0);
        min=savedData.getInt("Min",0);
        flagVkl=savedData.getBoolean("flag",false);
        //day="0";

        switchView=findViewById(R.id.switch_bn1);
        if (min<10){
            if (hour<10){
                switchView.setText("0"+hour+":0"+min+" Интервал ="+interval+" мин");
            }else{
                switchView.setText(hour+":0"+min+" Интервал ="+interval+" мин");
            }}else{
            if (hour<10){
                switchView.setText("0"+hour+":"+min+" Интервал ="+interval+" мин");
            }else{
                switchView.setText(hour+":"+min+" Интервал ="+interval+" мин");
            }
        }
        proverka();
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    budilka();
                   flagVklB=true;
                } else {
                    otmena();
                }
            }
        });
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private PendingIntent getAlarmInfoPendingIntent(){
      Intent alarmInfoIntent  = new Intent(this,MainActivity.class);
      alarmInfoIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
      return PendingIntent.getActivity(this,0,alarmInfoIntent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public PendingIntent getAlarmActionPendingIntent(){//Что произойдёт когда сработает будильник
        Intent intent = new Intent(this,AlarmActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this,1,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }
    @SuppressLint("SetTextI18n")
    public void budilka(){



        SharedPreferences savedData = getSharedPreferences("SavedData",MODE_PRIVATE);//сохранение данных

        hour=savedData.getInt("Hours",0);
        min=savedData.getInt("Min",0);
        if (min<10){
            if (hour<10){
                switchView.setText("0"+hour+":0"+min+" Интервал ="+interval+" мин");
            }else{
                switchView.setText(hour+":0"+min+" Интервал ="+interval+" мин");
            }}else{
            if (hour<10){
                switchView.setText("0"+hour+":"+min+" Интервал ="+interval+" мин");
            }else{
                switchView.setText(hour+":"+min+" Интервал ="+interval+" мин");
            }
        }

        MaterialTimePicker materialTimePicker =new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(min)

                .setTitleText("Выбирите время для будильника")
                .build();
        materialTimePicker.addOnPositiveButtonClickListener(view ->{


            Date currentTime = Calendar.getInstance().getTime();
if (currentTime.getHours()>materialTimePicker.getHour()) {
    zavtro(materialTimePicker,currentTime);
    }else {
        if (currentTime.getMinutes()>materialTimePicker.getMinute()){
            zavtro(materialTimePicker,currentTime) ;
            }else{
            segodny(materialTimePicker,currentTime) ;
            }
        }
            interval();
            SharedPreferences.Editor editor=savedData.edit();//сохранение данных
            editor.putInt("Hours", hour);
            editor.putInt("Min", min);
            editor.putBoolean("flag",flagVkl);
            editor.apply();
            Log.d("asd","min="+savedData.getInt("Min",0)+"Hours"+savedData.getInt("Hours",0));

            if(Objects.equals(day, "0")){
                switchView=findViewById(R.id.switch_bn1);

                if (min<10){
                    if (hour<10){
                        switchView.setText("0"+hour+":0"+min+" Интервал ="+interval+" мин");
                    }else{
                        switchView.setText(hour+":0"+min+" Интервал ="+interval+" мин");
                    }}else{
                    if (hour<10){
                        switchView.setText("0"+hour+":"+min+" Интервал ="+interval+" мин");
                    }else{
                        switchView.setText(hour+":"+min+" Интервал ="+interval+" мин");
                    }
                }
            }else {
                switchView = findViewById(R.id.switch_bn1);
                if (min<10){
                    if (hour<10){
                        switchView.setText("0"+hour+":0"+min+" Интервал ="+interval+" мин");
                    }else{
                        switchView.setText(hour+":0"+min+" Интервал ="+interval+" мин");
                    }}else{
                    if (hour<10){
                        switchView.setText("0"+hour+":"+min+" Интервал ="+interval+" мин");
                    }else{
                        switchView.setText(hour+":"+min+" Интервал ="+interval+" мин");
                    }
                }

            }
        });

        materialTimePicker.show(getSupportFragmentManager(),"Tag_piker");

    }
@SuppressLint("SetTextI18n")
public void otmena(){


        SharedPreferences savedData = getSharedPreferences("SavedData",MODE_PRIVATE);//сохранение данных

    hour=savedData.getInt("Hours",0);
    min=savedData.getInt("Min",00);

    switchView=findViewById(R.id.switch_bn1);
if (min<10){
    if (hour<10){
        switchView.setText("0"+hour+":0"+min+" Интервал ="+interval+" мин");
    }else{
        switchView.setText(hour+":0"+min+" Интервал ="+interval+" мин");
    }}else{
    if (hour<10){
        switchView.setText("0"+hour+":"+min+" Интервал ="+interval+" мин");
    }else{
        switchView.setText(hour+":"+min+" Интервал ="+interval+" мин");
    }
    }
    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    if (getAlarmActionPendingIntent()!=null){
    alarmManager.cancel(getAlarmActionPendingIntent());}//Отменяем будильник, связанный с интентом данного класса
   // Toast.makeText(this, "Будильник отменён", Toast.LENGTH_LONG).show();
    flagVkl=false;
    switchView.setChecked(flagVkl);
    SharedPreferences.Editor editor=savedData.edit();//сохранение данных
    editor.putInt("Hours", hour);
    editor.putInt("Min", min);
    editor.putBoolean("flag",flagVkl);
    editor.apply();
    }
    public void zavtro(MaterialTimePicker materialTimePicker,Date currentTime){
        flagVkl=true;
        calendar_zavtro.set(Calendar.SECOND, currentTime.getSeconds());
        calendar_zavtro.set(Calendar.MILLISECOND, 0);
        calendar_zavtro.set(Calendar.MINUTE, materialTimePicker.getMinute());
        calendar_zavtro.set(Calendar.HOUR_OF_DAY, materialTimePicker.getHour());

        calendar_zavtro.set(Calendar.DAY_OF_MONTH,currentTime.getDate()+1);
        day=calendar_zavtro.getTime().getDate();

        min = materialTimePicker.getMinute();//сохранение данных
        hour = materialTimePicker.getHour();

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar_zavtro.getTimeInMillis(), getAlarmInfoPendingIntent());
        alarmManager.setAlarmClock(alarmClockInfo, getAlarmActionPendingIntent());
        Toast.makeText(this, "Будильник установлен на: " + sdf.format(calendar_zavtro.getTime()), Toast.LENGTH_LONG).show();

    }
    public  void segodny(MaterialTimePicker materialTimePicker,Date currentTime){
        flagVkl=true;
        calendar_segodny.set(Calendar.SECOND, currentTime.getSeconds());
        calendar_segodny.set(Calendar.MILLISECOND, 0);
        calendar_segodny.set(Calendar.MINUTE, materialTimePicker.getMinute());
        calendar_segodny.set(Calendar.HOUR_OF_DAY, materialTimePicker.getHour());
        calendar_segodny.set(Calendar.DAY_OF_MONTH,currentTime.getDate());

        min = materialTimePicker.getMinute();//сохранение данных
        hour = materialTimePicker.getHour();
        day=calendar_segodny.getTime().getDate();


        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar_segodny.getTimeInMillis(), getAlarmInfoPendingIntent());
        alarmManager.setAlarmClock(alarmClockInfo, getAlarmActionPendingIntent());
        Toast.makeText(this, "Будильник установлен на: " + sdf.format(calendar_segodny.getTime()), Toast.LENGTH_LONG).show();



    }
    public void proverka(){
        switchView=findViewById(R.id.switch_bn1);
       switchView.setChecked(flagVkl);
    }
    public void razrysheniy(){
        int accessCoarseLocation = this.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int accessFineLocation   = this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
        int acessWRITEEXTERNALSTORAGE=this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listRequestPermission = new ArrayList<String>();

        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (acessWRITEEXTERNALSTORAGE != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listRequestPermission.isEmpty()) {
            String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
            this.requestPermissions(strRequestPermission, REQUEST_CODE_LOC);
        }
    }
    public void interval(){

     if (min-interval<0){
         min2=min-interval+60;
         if (hour-1<0){
             hour2=hour+23;
            day2=day-1;
         }else{
             hour2=hour-1;
             day2= day;
         }
     }else{
         min2=min-interval;
         hour2=hour;
         day2=day;
     }
    }
}



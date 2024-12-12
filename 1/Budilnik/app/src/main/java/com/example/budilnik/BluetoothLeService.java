package com.example.budilnik;

import static androidx.core.app.NotificationCompat.PRIORITY_DEFAULT;
import static androidx.core.app.NotificationCompat.PRIORITY_HIGH;
import static androidx.core.app.NotificationCompat.PRIORITY_MAX;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
public class BluetoothLeService extends Service {
    public float batteryPct=0;
    private final static String TAG = "123";
    private static final String DIR_SD = "MyFile";
    final String FILENAME = "LOG.txt";
    public byte[] data;
    private NotificationManager notificationManager;
    // Идентификатор уведомления
    private static final int NOTIFY_ID = 101;
    private static final int NOTIFYZU_ID = 102;
    private static final int NOTIFYZT_ID = 103;
    private static final int NOTIFYO_ID = 104;
    // Идентификаторы каналов уведомлений
    private static final String CHANNEL_ID = "Оповещения о входящих данных";
    private static final String CHANNELZU_ID = "Оповещение о малом уровне заряда пульсометра";
    private static final String CHANNELZT_ID = "Оповещение о недостаточном уровне заряда телефона";
    private static final String CHANNELO_ID = "Оповещение об отключении пульсометра от телефона";
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    public static int heartRate;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public final static String ACTION_GATT_CONNECTED =
            "android-er.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "android-er.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "android-er.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "android-er.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "android-er.EXTRA_DATA";
    public static Integer timer=0;
    private static final UUID Battery_Level_UUID =
            UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    public static String String_Name_ledService =
            "00001800-0000-1000-8000-00805f9b34fb";
    public static String String_Name_switchChar = "00002a00-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_Name_switchChare =
            UUID.fromString(String_Name_switchChar);
    public static String String_Puls_ledService =
            "0000180d-0000-1000-8000-00805f9b34fb";
    public final static ParcelUuid ParcelUuid_Puls_ledService =
            ParcelUuid.fromString(String_Puls_ledService);
    public static String String_Puls_switchChar = "00002a37-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_Puls_switchChare =
            UUID.fromString(String_Puls_switchChar);
    private byte zaryd=0;
    public BluetoothDevice device;
    private int priv=0;
    private boolean zarydFlag=true;
    private boolean zarydFlag2=true;


    @SuppressLint("MissingPermission")
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    @SuppressLint("MissingPermission")
    public void writeCharacteristic() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString(String_Name_ledService));
        if(mCustomService == null){
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }

        BluetoothGattCharacteristic characteristic1 = mCustomService.getCharacteristic(UUID_Name_switchChare);
        byte[] value = ControlActivity.Name_Device.getBytes();
        characteristic1.setValue(value);
        mBluetoothGatt.writeCharacteristic(characteristic1);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        writeCharacteristic();
        return START_REDELIVER_INTENT;
    }
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                notifykO();
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                try {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            try {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    @SuppressLint("SetTextI18n")
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) throws IOException {
        final Intent intent = new Intent(action);

        UUID uuid = characteristic.getUuid();
        if (Battery_Level_UUID.equals(uuid)) {
            data = characteristic.getValue();
            if (data != null && data.length > 0) {
                zaryd = data[0];
                intent.putExtra(EXTRA_DATA, new String(data, StandardCharsets.UTF_8) + "\n" + data[0]);
                Log.d(TAG, "Zaryd" + new String(data, StandardCharsets.UTF_8) + "  " + data[0]+"gh  "+batteryPct+"%");
            return;
            }
        } else if (UUID_Puls_switchChare.equals(uuid)) {
            if (timer >= 10) {
                writeCharacteristic();
                timer = 0;
                notifyk();
            } else {
                timer = timer + 1;
                Log.d(TAG, String.valueOf(timer));
            }
            if (MainActivity.flagVklB) {
                budilnik();
            }
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = this.registerReceiver(null, ifilter);
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryPct = level * 100 / (float)scale;
            Date cutTime = Calendar.getInstance().getTime();
            if (cutTime.getHours()>20){
            if (batteryPct<60){
                if (zarydFlag){
            notifykZT();
                zarydFlag=false;}
            }else {
                zarydFlag=true;
            }}
            if (zaryd<4){
                if (zarydFlag2){
                    notifykZP();
                    zarydFlag2=false;}
                else{
                    zarydFlag2=true;
                }
            }
            // проверяем доступность SD
            if (!Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                Log.d(TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
                return;
            }
            // получаем путь к SD
            File sdPath = Environment.getExternalStorageDirectory();
            // добавляем свой каталог к пути
            sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
            // создаем каталог
            sdPath.mkdirs();
            // формируем объект File, который содержит путь к файлу
            File sdFile = new File(sdPath, FILENAME);

            // отрываем поток для записи
            BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile, true));
            // пишем данные
            SimpleDateFormat sdf = new SimpleDateFormat("'Date :'dd.MM' and Time :'HH.mm.ss z");
            String currentDateAndTime = sdf.format(new Date());
            bw.write("Пульс "+heartRate + " :  " + currentDateAndTime +"  Заряд "+zaryd+ "  Заряд телефона "+batteryPct+ "%\n");
            // закрываем поток
            bw.close();
            Log.d(TAG, "Файл записан" + sdFile.getAbsolutePath());

            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));

        } else {// For all other profiles, writes the data formatted in HEX.
            data = characteristic.getValue();
            if (data != null && data.length > 0) {

                intent.putExtra(EXTRA_DATA, new String(data, StandardCharsets.UTF_8) + "\n" + data[0]);
                Log.d(TAG, "{{{}}}}" + new String(data, StandardCharsets.UTF_8) + "  " + data[0]);
            }
        }

        //remove special handling for time being
        Log.w(TAG, "broadcastUpdate()");



        sendBroadcast(intent);


            //ControlActivity.displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

    }
    public static void createChannelIfNeeded(NotificationManager manager){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel notificationChannel=new NotificationChannel(CHANNEL_ID,CHANNEL_ID, NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(notificationChannel);
            NotificationChannel notificationChannelZU=new NotificationChannel(CHANNELZU_ID,CHANNELZU_ID, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(notificationChannelZU);
            NotificationChannel notificationChannelZT=new NotificationChannel(CHANNELZT_ID,CHANNELZT_ID, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(notificationChannelZT);
            NotificationChannel notificationChannelO=new NotificationChannel(CHANNELO_ID,CHANNELO_ID, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(notificationChannelO);
    }
}
    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }
    private final IBinder mBinder = new LocalBinder();
    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }
    @SuppressLint("MissingPermission")
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;

        return true;
    }
    @SuppressLint("MissingPermission")
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }
    @SuppressLint("MissingPermission")
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    @SuppressLint("MissingPermission")
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled)
    {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_Puls_switchChare.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
    public void notifyk(){

    //readCustomCharacteristic();
    notificationManager=(NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    Intent intent1 = new Intent(getApplicationContext(),BluetoothLeService.class);
    intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent1,PendingIntent.FLAG_UPDATE_CURRENT);
    NotificationCompat.Builder notificationBulder=
            new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                    .setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pendingIntent)
                    .setContentTitle("Входные данные")
                    .setContentText("Пульс = "+ heartRate+"\nЗаряд = "+zaryd+"\nЗаряд телефона = "+batteryPct)
                    .setPriority(PRIORITY_MAX);
    createChannelIfNeeded(notificationManager);
    notificationManager.notify(NOTIFY_ID,notificationBulder.build());
    MainActivity.heartRate=heartRate;
    }
    public void notifykZP(){

        //readCustomCharacteristic();
        notificationManager=(NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(getApplicationContext(),BluetoothLeService.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent1,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBulder=
                new NotificationCompat.Builder(getApplicationContext(),CHANNELZU_ID)
                        .setAutoCancel(false)
                        .setSmallIcon(R.drawable.ic_chard_name)
                        .setWhen(System.currentTimeMillis())
                        .setContentIntent(pendingIntent)
                        .setContentTitle("Заряд пульсометра кончается")
                        .setContentText("Заряда пульсометра скорее всего не хватит на всю ночь")
                        .setPriority(PRIORITY_HIGH);
        createChannelIfNeeded(notificationManager);
        notificationManager.notify(NOTIFYZU_ID,notificationBulder.build());
    }
    public void notifykZT(){


        notificationManager=(NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(getApplicationContext(),BluetoothLeService.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent1,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBulder=
                new NotificationCompat.Builder(getApplicationContext(),CHANNELZT_ID)
                        .setAutoCancel(false)
                        .setSmallIcon(R.drawable.baseline_charging_station_24)
                        .setWhen(System.currentTimeMillis())
                        .setContentIntent(pendingIntent)
                        .setContentTitle("Заряд телефона")
                        .setContentText("Заряда телефона Скорее всего не хватит на всю ночь")
                        .setPriority(PRIORITY_MAX);
        createChannelIfNeeded(notificationManager);
        notificationManager.notify(NOTIFYZT_ID,notificationBulder.build());
    }
    public void notifykO(){


        notificationManager=(NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(getApplicationContext(),BluetoothLeService.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent1,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBulder=
                new NotificationCompat.Builder(getApplicationContext(),CHANNELO_ID)
                        .setAutoCancel(false)
                        .setSmallIcon(R.drawable.ic_chard_name_disconection)
                        .setWhen(System.currentTimeMillis())
                        .setContentIntent(pendingIntent)
                        .setContentTitle("Отключилось")
                        .setContentText("Пульсометр отключился от телефона.\n Для полноценной работы приложения подключите пульсометр обратно")
                        .setPriority(PRIORITY_DEFAULT);
        createChannelIfNeeded(notificationManager);
        notificationManager.notify(NOTIFYO_ID,notificationBulder.build());
    }
    public void budilnik(){
        if (MainActivity.flagVkl){
            Date cutTime = Calendar.getInstance().getTime();
           if (heartRate>=60){
               if (heartRate<=80){
                   if (priv>254){
               priv=0;
               if (MainActivity.day2<=cutTime.getDate()){
           if (MainActivity.hour2<=cutTime.getHours()){
               if (MainActivity.min2<=cutTime.getMinutes()){
                   MainActivity.flagVklB=false;
                   final Intent intent = new Intent(this,
                           AlarmActivity.class);
                   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                   startActivity(intent);
               }
                }
               }
                   }else {
                       priv=priv+1;
                   }
               }else {
                   priv=0;
               }
        }
        else {
               priv=0;
        }
        }
}

}
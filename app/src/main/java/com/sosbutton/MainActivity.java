package com.sosbutton;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView locationTextView;
    private TextView timeTextView;
    private static PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTextView = findViewById(R.id.locationTextView);
        timeTextView = findViewById(R.id.timeTextView);

        takeKeyEvents(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, ForegroundService.class));
        } else {
            startService(new Intent(this, ForegroundService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (wakeLock != null && wakeLock.isHeld()) {

            wakeLock.release();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        switch (keyCode) {
            case 85:
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


                    if (lastKnownLocation != null) {


                        double latitude = lastKnownLocation.getLatitude();
                        double longitude = lastKnownLocation.getLongitude();
                        String location = "Enlem: " + latitude + "\nBoylam: " + longitude;
                        locationTextView.setText(location);
                        Toast.makeText(getApplicationContext(),location, Toast.LENGTH_LONG).show();


                    } else {

                        locationTextView.setText("Hocam Konum Verisi Yok Söyle Geri Gelsin PLS");
                    }



                } else {
                    locationTextView.setText("Neden Konum İznini Vermedin Doktorrrrrrr.");
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String currentTime = sdf.format(new Date());
                timeTextView.setText(currentTime);

                return true;
        }

        return false;
    }

    public static class ForegroundService extends Service {
        private static final String CHANNEL_ID = "ForegroundServiceChannel";
        private final IBinder binder = new LocalBinder();

        @Override
        public void onCreate() {
            super.onCreate();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager manager = getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                }
            }

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Foreground Service")
                    .setContentText("Running")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            startForeground(1, notification);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SOSBUTTON:WakeLockTag");
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }

            return START_STICKY;
        }

        @Override
        public IBinder onBind(Intent intent) {
            return binder;
        }

        public class LocalBinder extends Binder {
            ForegroundService getService() {
                return ForegroundService.this;
            }
        }
    }
}
package comp5216.sydney.edu.au.runningdiary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;

import static comp5216.sydney.edu.au.runningdiary.StartMapActivity.TAG;


public class JoggingService extends Service {
    Location origin, destination;
    Marker userMarker;
    PlayerAdapter localPlayer;
    GoogleMap serviceMap;
    long startTime = 0, millis;
    int seconds,minutes;
    TextView timerText, distanceText;
    DecimalFormat df = new DecimalFormat("#.##");

    LocationManager manager;
    IBinder binder = new LocalBinder();
    float distance = 0f;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    public class LocalBinder extends Binder {

        public JoggingService getService() {
            return JoggingService.this;
        }
    }
    public JoggingService() {
    }

    public void setPlayerAdapter (PlayerAdapter player, String path){
        if(localPlayer != null) {
            localPlayer.release();
        }
        localPlayer = player;
        localPlayer.loadMedia(path);
    }
    public void setMap(GoogleMap map) {
        serviceMap = map;
        userMarker = serviceMap.addMarker(new MarkerOptions()
                .position(new LatLng(origin.getLatitude(), origin.getLongitude()
                )));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }
    //draw lines and marker on the map



    private LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //draw lines and marker to map
            destination = location;
            userMarker.setPosition(
                    new LatLng(destination.getLatitude(), destination.getLongitude()));

            serviceMap.addPolyline(new PolylineOptions()
                    .clickable(false)
                    .add(
                            new LatLng(origin.getLatitude(), origin.getLongitude()),
                            new LatLng(destination.getLatitude(), destination.getLongitude()
                            )));
            Log.d("GPS info", "running");
            Log.d("timer", String.format("%d:%02d", minutes, seconds) );

            //add distance per update (meters)
            distance += destination.distanceTo(origin);
            distanceText.setText(df.format(distance));
            Log.d("distance  = ", String.valueOf(distance));
            origin = destination;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Log.e(TAG, "onProviderDisabled: " + provider);

        }
    };

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            millis = System.currentTimeMillis() - startTime;
            seconds = (int) (millis / 1000);
            minutes = seconds / 60;
            seconds = seconds % 60;


            timerText.setText(String.format("%d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };
    public void startTimer(TextView tv) {
        timerText = tv;
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

    }
    public void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }
    public void setDistance (TextView tv){
        distanceText = tv;
    }

    @SuppressLint("MissingPermission")
    public void startRunning() {
        getLocationUpdate();
    }
    private void getLocationUpdate() {
        manager = (LocationManager) getApplicationContext().
                getSystemService(Context.LOCATION_SERVICE);
        try {
            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 2000, 0,listener);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }
    public void stopRunning() {
        if(manager != null){
            manager.removeUpdates(listener);
        }
    }

//    @Override
//    public void onDestroy(){
//        super.onDestroy();

//    }




}

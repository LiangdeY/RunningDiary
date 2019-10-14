package comp5216.sydney.edu.au.runningdiary;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StartMapActivity extends FragmentActivity implements OnMapReadyCallback {
    //static stuff
    public static final String TAG = "Log";
    static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;
    //private static final int PERMISSIONS_READ_EXTERNAL_STORAGE = 101;

    private static final int REQUEST_CHANGE_SONG = 101;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private static final float DEFAULT_ZOOM = 18f;
    private boolean mLocationPermissionGranted;
    private boolean mUserIsSeeking = false;
    //map stuff
    private CameraPosition mCameraPosition;
    private Location mCurrentLocation, mLastKnownLocation;
    private LatLng mDefaultLatLng = new LatLng(-34, 151);
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean isRunning = false;
    private GoogleMap mMap;
    //song stuff
    private TextView mTextSongName;
    private TextView timerText;
    private TextView distanceText;

    private SeekBar mSeekbarAudio;
    private PlayerAdapter mPlayerAdapter;
    private String currentSongName, currentSongPath;
    private HashMap<String, String> audioMap;


    JoggingService mService;
    boolean mBound = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        initializeMap(savedInstanceState);

            initializePlayList();
            //setup name and absolute path to song
            setDefaultSong();
            initializeUI();
            initializeSeekbar();
            initializePlaybackController();

        Log.d("onCreate" , "onCreate success");
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Bind to JoggingService
        Intent intent = new Intent(this, JoggingService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

    }
    @Override
    protected void onStop() {
        super.onStop();
        if (isChangingConfigurations() && mPlayerAdapter.isPlaying()) {
            Log.d(TAG, "onStop: don't release MediaPlayer as screen is rotating & playing");
        } else {
            //mPlayerAdapter.release();
            Log.d(TAG, "onStop: release MediaPlayer");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CHANGE_SONG) {
            if(resultCode == RESULT_OK){
               currentSongPath = audioMap.get(data.getStringExtra("selectedName"));
                mTextSongName.setText(data.getStringExtra("selectedName"));
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    //@Override
    //protected void onDestroy(){

   // }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(mCurrentLocation == null) {
            mMap.addMarker(new MarkerOptions().position(mDefaultLatLng).title("Sydney"));
        }else {
            mMap.addMarker(new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(),
                    mCurrentLocation.getLongitude())).title("Marker in Sydney"));
        }
        if(mCameraPosition == null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLatLng, DEFAULT_ZOOM));

        }else{
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        }
        if(mService != null && mService.serviceMap != null) {
            mMap = mService.serviceMap;
        }

        updateLocationUI();
        getDeviceLocation();
    }

    public void onStartBtnClick(View v) {

            if (mBound && !isRunning) {
                mService.origin = mLastKnownLocation;
                mService.setMap(mMap);
                mService.startTimer( timerText );
                mService.setDistance( distanceText );
                mService.startRunning();
                Log.d(TAG, "onStartBtnClick: Service Start");
                isRunning = true;
            }
    }

    public void onFinishBtnClick(View v) {
        if(isRunning) {
            //unbind Service
            mService.stopTimer();
            mService.stopRunning();
            Intent intent = new Intent(this, FinishRunningActivity.class);
            String str_dis, str_time;
            str_dis = distanceText.getText().toString();
            Log.d("distance1 = ", str_dis);
            str_time = timerText.getText().toString();
            float minutes = mService.minutes + mService.seconds / 60f;
            //meters to miles
            float flo_dis = Float.valueOf(str_dis) * 0.0006f;
            Log.d("distance2 = ", String.valueOf(flo_dis));

            float pace ;
            if(flo_dis == 0f) {
                pace = 0f;
            }else{
                //return Mins per mile
                pace = minutes / flo_dis;
            }
            //return miles per hour
            float speed;

            if(minutes == 0f) {
                speed = 0f;
            }else{
                speed = flo_dis / (minutes / 60f);
            }

            //DecimalFormat df = new DecimalFormat("#.###");

            //passing strings
            intent.putExtra("distance",String.valueOf(flo_dis));
            intent.putExtra("time", str_time);
            intent.putExtra("pace", String.valueOf(pace));
            intent.putExtra("speed",String.valueOf(speed));
            startActivity(intent);

            //unbind the service
            try {
                unbindService(connection);
                mBound = false;
                Log.d("unbind success","mBound = false");

            }
            catch (Exception e) {
                Log.d("unbind failes", e.toString());
            }
        }
    }

        /*
     * map stuff     */
    private void initializeMap(Bundle savedInstanceState) {
        //retrieve the device's location and the map's camera position if previously saved:
        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

    }
    public void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            updateLocationUI();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
//    public void getAudioAccessPermission() {
//        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
//                Manifest.permission.READ_EXTERNAL_STORAGE)
//                == PackageManager.PERMISSION_GRANTED) {
//           //do nothing
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
//                    PERMISSIONS_READ_EXTERNAL_STORAGE);
//        }
//    }

    //Override the onRequestPermissionsResult() callback to handle the permission requested:
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getDeviceLocation();

                }
            }

        }
        updateLocationUI();
    }
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            Log.d("Task", "Task successful");

                            mLastKnownLocation = (Location) task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d("No Location", "Location is null. Using defaults.");
                            Log.e("Exception", "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    mDefaultLatLng, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                //mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                Log.d("locationBtn" , "locationBtn is disabled");

                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /*
     *music player stuff
     */
    private void initializeUI() {
        mTextSongName = (TextView) findViewById(R.id.text_song_name);
        Button mPlayButton = (Button) findViewById(R.id.button_play);
        Button mPauseButton = (Button) findViewById(R.id.button_pause);
        mSeekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);
        timerText = (TextView) findViewById(R.id.timerText);
        distanceText = (TextView) findViewById(R.id.distanceText);


        if(mBound) {
            timerText.setText(String.format("%d:%02d", mService.minutes, mService.seconds));
        }

        mPauseButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mService.localPlayer != null) {
                            mService.localPlayer.release();
                        }
                    }
                });
        mPlayButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mService.setPlayerAdapter(mPlayerAdapter, currentSongPath);
                        mService.localPlayer.play();
                    }
                });
        mTextSongName.setText(currentSongName);
    }
    private void initializeSeekbar() {
        mSeekbarAudio.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int userSelectedPosition = 0;

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = true;
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            userSelectedPosition = progress;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = false;
                        mPlayerAdapter.seekTo(userSelectedPosition);
                    }
                });
    }
    private void initializePlayList() {
       // getAudioAccessPermission();
        try{
//            if(mReadExternalStoragePermissionGranted){
                audioMap = new HashMap<>();
                //specify  are the contents we are going to retrieve
                String[] mProjections  = { MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.DISPLAY_NAME };
                //set the projection and make a query for data
                Cursor audioCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        mProjections, null, null, null);

                //add that data into a list.
                if(audioCursor != null){
                    if(audioCursor.moveToFirst()){
                        do{
                            int nameIndex = audioCursor.
                                    getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
                            int pathIndex = audioCursor.
                                    getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                            audioMap.put(audioCursor.getString(nameIndex), audioCursor.getString(pathIndex));
                        }while(audioCursor.moveToNext());
                    }
                }
                audioCursor.close();
        }catch (Exception e) {

        }
    }
    private void initializePlaybackController() {
        MediaPlayerHolder mMediaPlayerHolder = new MediaPlayerHolder(this);
        Log.d(TAG, "initializePlaybackController: created MediaPlayerHolder");
        mMediaPlayerHolder.setPlaybackInfoListener(new PlaybackListener());
        mPlayerAdapter = mMediaPlayerHolder;
        Log.d(TAG, "initializePlaybackController: MediaPlayerHolder progress callback set");
    }
    private void setDefaultSong() {
        if(currentSongPath == null) {
            ArrayList<String> path  =  new ArrayList<>(audioMap.values());
            currentSongPath = path.get(0);
            currentSongName = getKeyByValue(audioMap, currentSongPath);
        }
    }


    public class PlaybackListener extends PlaybackInfoListener {

        @Override
        public void onDurationChanged(int duration) {
            mSeekbarAudio.setMax(duration);
            Log.d(TAG, String.format("setPlaybackDuration: setMax(%d)", duration));
        }

        @Override
        public void onPositionChanged(int position) {
            if (!mUserIsSeeking) {
                mSeekbarAudio.setProgress(position, true);
                Log.d(TAG, String.format("setPlaybackPosition: setProgress(%d)", position));
            }
        }

        @Override
        public void onStateChanged(@State int state) {
            String stateToString = PlaybackInfoListener.convertStateToString(state);
            onLogUpdated(String.format("onStateChanged(%s)", stateToString));
        }

        @Override
        public void onPlaybackCompleted() {
        }

    }


    public void onNextBtnClick(View v) {
        Intent intent = new Intent(this, ListMusicActivity.class);
        intent.putStringArrayListExtra("songName", new ArrayList<>(audioMap.keySet()));
        startActivityForResult(intent, REQUEST_CHANGE_SONG);
    }

    public static <T, E> T getKeyByValue(HashMap<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private ServiceConnection connection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            JoggingService.LocalBinder binder = (JoggingService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}

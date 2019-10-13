package comp5216.sydney.edu.au.runningdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ListMusicActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101;

    private List<String> nameList;
    private ListView audioView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        getSongList(savedInstanceState);
        getReadExternalStoragePermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeSongList();
                }
            }
        }
    }

    //get the list of the external storage sent from map activity
    private void getSongList(Bundle b) {

        if (b == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                nameList = null;
            } else {
                nameList= extras.getStringArrayList("songName");
            }
        } else {
            nameList = (ArrayList) b.getSerializable("nameList");
        }

    }

    private void getReadExternalStoragePermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            initializeSongList();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    public void onBackBtnClick(View v) {
        Intent intent = new Intent(this, StartMapActivity.class);
    }

    //initialzie the list view, send the name of clicked item back
    private void initializeSongList() {
        audioView = findViewById(R.id.songView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,android.R.layout.simple_list_item_1,android.R.id.text1, nameList);
        audioView.setAdapter(adapter);
        audioView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedName = (String) audioView.getAdapter().getItem(position);
                Intent intent = new Intent(ListMusicActivity.this, StartMapActivity.class);
                intent.putExtra("selectedName", selectedName);
                setResult(Activity.RESULT_OK,intent);
                finish();
            }
        });
    }


}

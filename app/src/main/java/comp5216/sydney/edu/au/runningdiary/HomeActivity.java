package comp5216.sydney.edu.au.runningdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;


public class HomeActivity extends AppCompatActivity {
    LogDB db;
    LogDao logDao;
    ListView listView;
    ArrayAdapter<String[]> adapter;
    List<String[]> logList;
    private static final int PERMISSIONS_READ_EXTERNAL_STORAGE = 101;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeDataBase();
        initializeListView();
        Log.d("HomeActuvity", "successfully init database and listView");
        Bundle extras = getIntent().getExtras();
        String[] item = new String[2];
        try{

            item[0] = extras.getString("date");
            item[1] = extras.getString("statistics");
            if(item[0] != null && item[1] != null) {
                logList.add(0,item);
                adapter.notifyDataSetChanged();
                saveItemsToDatabase();
            }
            else{
                Log.d("null Data:", item[0] + "|" + item[1]);
            }
        }catch (Exception e) {
            Log.d("Intent.getExtra exceptions", e.toString());
        }
        getAudioAccessPermission();
    }

    public void onPaceCakBtnClick(View v) {
        Intent intent = new Intent(this, CalculatePaceActivity.class);
        startActivity(intent);
    }
    public void onMapBtnClick(View v) {
        Intent intent = new Intent(this, StartMapActivity.class);
        startActivity(intent);
    }
        public void getAudioAccessPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
           //do nothing
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_READ_EXTERNAL_STORAGE);
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                  //  mLocationPermissionGranted = true;
                }
            }
//            break;
//            case PERMISSIONS_READ_EXTERNAL_STORAGE: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    //mReadExternalStoragePremissionGranted = true;
//                }
//            }
        }
    }

    private void readItemsFromDatabase() {
        //Use asynchronous task to run query on the background and wait for result
        try {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    //read items from database
                    List<LogItem> itemsFromDB = logDao.listAll();
                    logList = new LinkedList<String[]>();
                    if (itemsFromDB != null & itemsFromDB.size() > 0) {
                        for (LogItem item : itemsFromDB) {
                            String [] data = new String[] {item.getDate(), String.valueOf(item.getStatistics())};
                            logList.add(data);
                            Log.i("SQLite read item", "ID: " + item.getLogID() + " Name: " + item.getDate());
                        }
                    }
                    return null;
                }
            }.execute().get();
        }
        catch(Exception ex) {
            Log.e("readItemsFromDatabase", ex.getStackTrace().toString());
        }
    }
    private void saveItemsToDatabase() {
        //Use asynchronous task to run query on the background to avoid locking UI
        new AsyncTask<Void, Void, Void>() { @Override
        protected Void doInBackground(Void... voids) { //delete all items and re-insert
            logDao.deleteAll();
            for (String[] data : logList) {
                LogItem item = new LogItem(data[0],data[1]);
                logDao.insert(item);
                Log.i("SQLite saved item", data[0] + data[1]);
            }
            return null; }
        }.execute();
    }
    private void initializeListView() {
        listView = (ListView) findViewById(R.id.logView);
        readItemsFromDatabase();
        adapter = new ArrayAdapter<String[]>(this, android.R.layout.simple_expandable_list_item_2, android.R.id.text1, logList){
            @Override
            public  View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                String[] entry = logList.get(position);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text1.setText(entry[0]);
                text2.setText(entry[1]);

                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.height = 200;
                view.setLayoutParams(params);

                return view;
            }
        };
        listView.setAdapter(adapter);
    }
    private void initializeDataBase() {
        db = LogDB.getDatabase(this.getApplication().getApplicationContext());
        logDao = db.logDao();
    }
}

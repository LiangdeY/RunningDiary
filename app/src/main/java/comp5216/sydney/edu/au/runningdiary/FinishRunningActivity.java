package comp5216.sydney.edu.au.runningdiary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Date;

public class FinishRunningActivity extends AppCompatActivity {
    private TextView dateText, speedText, paceText, distaceText, timeText;
    private String date, speed, pace, distance, time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_running);
        initializeUI();
    }
    private void initializeUI() {
        Bundle extras = getIntent().getExtras();
        try {
            speed = extras.getString("speed");
            pace = extras.getString("pace");
            distance = extras.getString("distance");
            time = extras.getString("time");
            date = new Date().toString();
        }catch (Exception e ){
            Log.d("Exception", e.toString());
        }
        dateText  = findViewById(R.id.f_date);
        dateText.setText(date);
        speedText  = findViewById(R.id.f_speed);
        speedText.setText(speed);
        paceText  = findViewById(R.id.f_pace);
        paceText.setText(pace);
        distaceText  = findViewById(R.id.f_distance);
        distaceText.setText(distance);
        timeText  = findViewById(R.id.f_time);
        timeText.setText(time);
    }

}

package comp5216.sydney.edu.au.runningdiary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.text.format.DateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

public class FinishRunningActivity extends AppCompatActivity {
    private TextView dateText, speedText, paceText, distaceText, timeText;
    private String date, speed, pace, distance, time;
    Button completeBtn;
    DateFormat df;

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
            //date = new Date().toString();
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

        date = df.format("yyyy-MM-dd hh:mm:ss a", new java.util.Date()).toString();

        completeBtn  = findViewById(R.id.completeBtn);
        completeBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String statistics = "Distance:" + distance + " miles | "
                                + "Time:" + time + " | "
                                + "Pace:" + pace + " mins/mile | "
                                + "Speed:" + speed + " miles/hour |";
                        Intent intent = new Intent(
                                FinishRunningActivity.this, HomeActivity.class);
                        intent.putExtra("date", date);
                        intent.putExtra("statistics", statistics);
                        startActivity(intent);
                    }
                });
    }

}

package comp5216.sydney.edu.au.runningdiary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class CalculatePaceActivity extends AppCompatActivity {
    private float pace, speed;

    private EditText inputDistance;
    private EditText inputTime;
    private TextView outputPace;
    private TextView outputSpeed;

    private TextWatcher textWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
            updateText();
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_pace);


        //setup
        inputDistance = findViewById(R.id.editDistance);
        inputTime = findViewById(R.id.editTime);
        //add textWatcher to get runtime update
        inputDistance.addTextChangedListener(textWatcher);
        inputTime.addTextChangedListener(textWatcher);

        outputPace = findViewById(R.id.paceView);
        outputSpeed= findViewById(R.id.speedView);
    }

    public void calPaceNSpeed() {
        float time, distance;
        time = toFloat(inputTime);
        distance = toFloat(inputDistance);

        try {
            //return mins per mile
            pace = time / distance;
            //return miles per hour
            speed = distance / (time / 60f);
        } catch (ArithmeticException e) {
            //catch divide by zero exception
            Toast.makeText(this, "Can't divide by 0", Toast.LENGTH_SHORT).show();
            pace = 0f;
            speed = 0f;
        }
    }

    public float toFloat(EditText edit) {
        //handle invalid input
        String s  = edit.getText().toString();
        if (s.matches("")) {
            Toast.makeText(this, " Text cannot be blank", Toast.LENGTH_SHORT).show();
            return 0f;
        }
        else{
            return Float.valueOf(s);
        }
    }

    public void updateText() {
        calPaceNSpeed();

        //format and show the text
        DecimalFormat df = new DecimalFormat("#.##");
        outputPace.setText(df.format(pace));
        outputSpeed.setText(df.format(speed));
    }

//end of class
}

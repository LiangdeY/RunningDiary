package comp5216.sydney.edu.au.runningdiary;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "logList")
public class LogItem {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "logID")
    private int logID;

    @ColumnInfo(name = "time")
    private String time;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "pace")
    private float pace;

    @ColumnInfo(name = "speed")
    private float speed;

    @ColumnInfo(name = "distance")
    private float distance;

    @ColumnInfo(name = "statistics")
    private String statistics;

    public LogItem(String date, String statistics){
        this.date = date;
        this.statistics = statistics;
    }

    public int getLogID() { return logID; }
    public void setLogID(int logID) {
        this.logID = logID;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) { this.time = time; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public float getPace() { return pace; }
    public void setPace(float pace) { this.pace = pace; }

    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed= speed; }

    public float getDistance() { return distance; }
    public void setDistance(float distance) { this.distance = distance; }

    public String getStatistics() { return statistics; }
    public void setStatistics(String statistics) { this.statistics = statistics; }

    public void buildStatistics() {
        statistics = "Distance:" + distance + " | " +
                "Time:" + time + " | " +
                "Pace:" + pace + " | " +
                "Speed:" + speed + " | ";
    }
}

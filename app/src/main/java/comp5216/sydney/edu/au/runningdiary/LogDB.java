package comp5216.sydney.edu.au.runningdiary;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LogItem.class}, version = 1, exportSchema = false)
public abstract class LogDB extends RoomDatabase {
    private static final String DATABASE_NAME = "logItem_db";
    private static LogDB DBINSTANCE;

    public abstract LogDao logDao();

    public static LogDB getDatabase(Context context) {
        if (DBINSTANCE == null) {
            synchronized (LogDB.class) {
                DBINSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        LogDB.class, DATABASE_NAME).build();
            }
        }
        return DBINSTANCE;
    }

    public static void destroyInstance() {
        DBINSTANCE = null;
    }
}

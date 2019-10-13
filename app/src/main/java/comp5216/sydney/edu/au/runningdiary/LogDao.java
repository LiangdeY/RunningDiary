package comp5216.sydney.edu.au.runningdiary;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LogDao {
    @Query("SELECT * FROM logList")
    List<LogItem> listAll();

    @Insert
    void insert(LogItem toDoItem);

    @Insert
    void insertAll(LogItem... toDoItems);

    @Query("DELETE FROM logList")
    void deleteAll();
}

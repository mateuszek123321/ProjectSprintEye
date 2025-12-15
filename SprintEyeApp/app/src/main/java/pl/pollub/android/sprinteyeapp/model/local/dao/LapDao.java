package pl.pollub.android.sprinteyeapp.model.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pl.pollub.android.sprinteyeapp.model.local.entity.Lap;

@Dao
public interface LapDao {
    @Insert
    long insert(Lap lap);

    @Insert
    List<Long> InsertAll(List<Lap> laps);

    @Update
    void update(Lap lap);
    @Delete
    void delete(Lap lap);

    @Query("SELECT * FROM lap WHERE lap_id = :id LIMIT 1")
    Lap getById(long id);

    @Query("SELECT * FROM lap " +
            "WHERE run_id = :runId " +
            "ORDER BY lap_number ASC")
    LiveData<List<Lap>> observeLapsForRun(long runId);

    @Query("SELECT * FROM lap WHERE run_id = :runId ORDER BY lap_number ASC")
    List<Lap> getLapsForRun(long runId);

    @Query("DELETE FROM lap WHERE run_id = :runId")
    void deleteLapsForRun(long runId);
}

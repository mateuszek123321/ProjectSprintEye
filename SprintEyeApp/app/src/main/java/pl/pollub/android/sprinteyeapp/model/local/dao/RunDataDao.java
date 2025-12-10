package pl.pollub.android.sprinteyeapp.model.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import pl.pollub.android.sprinteyeapp.model.local.entity.RunData;
import pl.pollub.android.sprinteyeapp.model.local.relation.RunWithLaps;

@Dao
public interface RunDataDao {
    @Insert
    long insert(RunData run);

    @Update
    void update(RunData run);

    @Delete
    void delete(RunData run);

    @Query("SELECT * FROM run_data Where run_id = :id LIMIT 1")
    RunData getById(long id);

    @Query("SELECT * FROM run_data " +
            "WHERE session_id = :sessionId " +
            "ORDER BY run_order ASC")
    LiveData<List<RunData>> observeRunsForSession(long sessionId);

    @Query("SELECT * FROM run_data " +
            "WHERE athlete_id = :athleteId " +
            "ORDER BY run_id DESC")
    LiveData<List<RunData>> observeRunsForAthlete(long athleteId);

    @Transaction
    @Query("SELECT * FROM run_data WHERE run_id = :id")
    LiveData<RunWithLaps> observeRunWithLaps(long id);
}

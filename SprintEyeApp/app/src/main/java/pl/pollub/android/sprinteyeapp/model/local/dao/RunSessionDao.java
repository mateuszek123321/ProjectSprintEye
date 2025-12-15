package pl.pollub.android.sprinteyeapp.model.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import pl.pollub.android.sprinteyeapp.model.local.entity.RunSession;
import pl.pollub.android.sprinteyeapp.model.local.relation.RunSessionWithRuns;
import pl.pollub.android.sprinteyeapp.model.local.relation.RunSessionWithRunsAndLaps;

@Dao
public interface RunSessionDao {
    @Insert
    long insert(RunSession session);

    @Update
    void update(RunSession session);

    @Delete
    void delete(RunSession session);

    @Query("SELECT * FROM run_session WHERE session_id = :id LIMIT 1")
    RunSession getById(long id);

    @Query("SELECT * FROM run_session " +
        "WHERE account_user_id = :userId " +
        "ORDER By created_at DESC")
    LiveData<List<RunSession>> observeSessionsForUser(long userId);

    @Transaction
    @Query("SELECT * FROM run_session WHERE session_id = :id")
    LiveData<RunSessionWithRuns> observeSessionWithRuns(long id);

    @Transaction
    @Query("SELECT * FROM run_session WHERE session_id = :id")
    LiveData<RunSessionWithRunsAndLaps> observeSessionWithRunsAndLaps(long id);
    @Query("SELECT * FROM run_session WHERE account_user_id = :userId ORDER BY created_at DESC")
    List<RunSession> getSessionsForUser(long userId);

    @Query("DELETE FROM run_session WHERE session_id = :id")
    void deleteById(long id);
}

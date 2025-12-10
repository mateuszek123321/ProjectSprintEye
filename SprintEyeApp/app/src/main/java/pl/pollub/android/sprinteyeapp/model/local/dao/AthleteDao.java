package pl.pollub.android.sprinteyeapp.model.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Index;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.lang.annotation.Target;
import java.util.List;

import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;
import pl.pollub.android.sprinteyeapp.model.local.relation.AthleteWithRuns;

@Dao
public interface AthleteDao {
    @Insert
    long insert(Athlete athlete);

    @Update
    void update(Athlete athlete);

    @Delete
    void delete(Athlete athlete);

    @Query("SELECT * FROM athlete WHERE account_user_id = :userId")
    List<Athlete> getAthletesForUser(long userId);

    @Query("SELECT * FROM athlete WHERE athlete_id = :id LIMIT 1")
    Athlete getById(long id);

    @Query("SELECT COUNT(*) FROM athlete WHERE athlete_nick =:athlete_nick AND account_user_id =:userId AND (:excludeId < 0 OR athlete_id != :excludeId)")
    int countByNick(String athlete_nick, long userId, long excludeId);
    @Transaction
    @Query("SELECT * FROM athlete WHERE athlete_id = :id")
    LiveData<AthleteWithRuns> observeAthleteWithRuns(long id);

    @Transaction
    @Query("SELECT * FROM athlete WHERE account_user_id = :userId ORDER BY created_at DESC")
    LiveData<List<Athlete>> observeAthletesForUser(long userId);

}

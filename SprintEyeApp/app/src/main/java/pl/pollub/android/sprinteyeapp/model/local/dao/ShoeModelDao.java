package pl.pollub.android.sprinteyeapp.model.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import pl.pollub.android.sprinteyeapp.model.local.entity.ShoeModel;

@Dao
public interface ShoeModelDao {

    @Insert
    long insert(ShoeModel shoe);
    @Insert
    List<Long> insertAll(List<ShoeModel> shoes);

    @Update
    void update(ShoeModel shoe);

    @Delete
    void delete(ShoeModel shoe);

    @Query("SELECT * FROM shoe_model " +
    "WHERE athlete_id = :athleteId " +
    "ORDER BY shoe_name ASC")
    LiveData<List<ShoeModel>> observeShoesForAthlete(long athleteId);

    @Query("DELETE FROM shoe_model WHERE athlete_id = :athleteId")
    void deleteForAthlete(long athleteId);

    @Query("SELECT * FROM shoe_model WHERE athlete_id = :athleteId")
    List<ShoeModel> getShoesForAthlete(long athleteId);
    @Query("DELETE FROM shoe_model")
    void deleteAll();
}

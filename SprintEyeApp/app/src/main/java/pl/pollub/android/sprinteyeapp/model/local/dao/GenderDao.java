package pl.pollub.android.sprinteyeapp.model.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import pl.pollub.android.sprinteyeapp.model.local.entity.Gender;

@Dao
public interface GenderDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Gender gender);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    List<Long> insertAll(List<Gender> genders);

    @Query("SELECT * FROM gender ORDER BY gender_id")
    List<Gender> getAll();

    @Query("SELECT * FROM gender WHERE gender_id = :id LIMIT 1")
    Gender getById(long id);
}

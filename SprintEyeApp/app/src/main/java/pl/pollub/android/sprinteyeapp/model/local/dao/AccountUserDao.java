package pl.pollub.android.sprinteyeapp.model.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import pl.pollub.android.sprinteyeapp.model.local.entity.AccountUser;
import pl.pollub.android.sprinteyeapp.model.local.relation.AccountUserWithAthletes;
import pl.pollub.android.sprinteyeapp.model.local.relation.AccountUserWithSessions;

@Dao
public interface AccountUserDao {
    @Insert
    long insert(AccountUser user);

    @Update
    void update(AccountUser user);

    @Query("SELECT * FROM account_user WHERE account_user_id = :id")
    AccountUser getById(long id);

    @Query("DELETE FROM account_user")
    void deleteAll();

    @Query("SELECT * FROM account_user WHERE email = :email LIMIT 1")
    AccountUser findByEmail(String email);

    @Query("SELECT * FROM account_user WHERE user_name = :userName LIMIT 1")
    AccountUser findByUserName(String userName);

    @Transaction
    @Query("SELECT * FROM account_user WHERE account_user_id = :id")
    LiveData<AccountUserWithAthletes> observeUserWithAthletes(long id);

    @Transaction
    @Query("SELECT * FROM account_user WHERE account_user_id = :id")
    LiveData<AccountUserWithSessions> observeUserWithSessions(long id);
}

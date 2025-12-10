package pl.pollub.android.sprinteyeapp.model.local.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import pl.pollub.android.sprinteyeapp.model.local.entity.AccountUser;
import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;

public class AccountUserWithAthletes {
    @Embedded
    public AccountUser accountUser;
    @Relation(
            parentColumn = "account_user_id",
            entityColumn = "account_user_id"
    )
    public List<Athlete> athletes;
}

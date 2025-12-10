package pl.pollub.android.sprinteyeapp.model.local.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;
import pl.pollub.android.sprinteyeapp.model.local.entity.ShoeModel;

public class AthleteWithShoes {
    @Embedded
    public Athlete athlete;
    @Relation(
            parentColumn = "athlete_id",
            entityColumn = "athlete_id"
    )
    public List<ShoeModel> shoes;
}

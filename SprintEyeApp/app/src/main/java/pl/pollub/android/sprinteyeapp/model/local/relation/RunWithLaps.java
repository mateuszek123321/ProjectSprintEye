package pl.pollub.android.sprinteyeapp.model.local.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import pl.pollub.android.sprinteyeapp.model.local.entity.Lap;
import pl.pollub.android.sprinteyeapp.model.local.entity.RunData;

public class RunWithLaps {
    @Embedded
    public RunData runData;
    @Relation(
            parentColumn = "run_id",
            entityColumn = "run_id"
    )
    public List<Lap> laps;
}

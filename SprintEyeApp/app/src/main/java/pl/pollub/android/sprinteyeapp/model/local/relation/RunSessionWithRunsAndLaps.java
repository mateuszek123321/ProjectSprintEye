package pl.pollub.android.sprinteyeapp.model.local.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import pl.pollub.android.sprinteyeapp.model.local.entity.RunData;
import pl.pollub.android.sprinteyeapp.model.local.entity.RunSession;

public class RunSessionWithRunsAndLaps {
    @Embedded
    public RunSession session;
    @Relation(
            entity = RunData.class,
            parentColumn = "session_id",
            entityColumn = "session_id"
    )
    public List<RunWithLaps> runs;
}

package pl.pollub.android.sprinteyeapp.model.local.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import pl.pollub.android.sprinteyeapp.model.local.entity.RunData;
import pl.pollub.android.sprinteyeapp.model.local.entity.RunSession;

public class RunSessionWithRuns {
    @Embedded
    public RunSession session;

    @Relation(
            parentColumn = "session_id",
            entityColumn = "session_id"
    )
    public List<RunData> runs;
}

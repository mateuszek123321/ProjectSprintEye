package pl.pollub.android.sprinteyeapp.model.local.entity;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "run_data",
foreignKeys = {
        @ForeignKey(
                entity = RunSession.class,
                parentColumns = "session_id",
                childColumns = "session_id",
                onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
                entity = Athlete.class,
                parentColumns = "athlete_id",
                childColumns = "athlete_id",
                onDelete = ForeignKey.CASCADE
        )
}, indices = {
        @Index("session_id"),
        @Index("athlete_id")
})
public class RunData {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "run_id")
    public long id;
    @ColumnInfo(name = "run_order")
    public int runOrder;

    @ColumnInfo(name = "run_time_ms")
    public long runTimeMs;

    @ColumnInfo(name = "session_id")
    public long sessionId;

    @ColumnInfo(name = "athlete_id")
    public long athleteId;
}

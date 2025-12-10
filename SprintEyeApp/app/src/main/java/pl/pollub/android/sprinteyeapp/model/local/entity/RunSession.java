package pl.pollub.android.sprinteyeapp.model.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "run_session",
foreignKeys = @ForeignKey(
        entity = AccountUser.class,
        parentColumns = "account_user_id",
        childColumns = "account_user_id",
        onDelete = ForeignKey.CASCADE
),
indices = {
        @Index("account_user_id")
})
public class RunSession {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "session_id")
    public long id;

    @NonNull
    @ColumnInfo(name = "session_type")
    public String sessionType;

    @NonNull
    @ColumnInfo(name = "attempt_name")
    public String attemptName;

    @ColumnInfo(name = "planned_distance")
    public int plannedDistance;
    @ColumnInfo(name = "created_at")
    public long createdAt;

    //detection distance mode
    @ColumnInfo(name = "detection_distance_mode")
    public boolean detectionDistanceMode;
    @ColumnInfo(name = "detection_lap_distance")
    public Integer detectionLapDistance;
    @ColumnInfo(name = "detection_expected_count")
    public Integer detectionExpectedCount;

    //series mode
    @ColumnInfo(name = "series_mode")
    public boolean seriesMode;
    @ColumnInfo(name = "series_lap_distance")
    public Integer seriesLapDistance;
    @ColumnInfo(name = "series_count")
    public Integer seriesCount;
    @ColumnInfo(name = "detections_per_series")
    public Integer detectionPerSeries;
    @ColumnInfo(name = "time_between_series")
    public Integer timeBetweenSeries;
    @ColumnInfo(name = "account_user_id")
    public long accountUserId;
}

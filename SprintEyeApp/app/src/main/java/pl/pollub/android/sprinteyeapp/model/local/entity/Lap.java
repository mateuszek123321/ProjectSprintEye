package pl.pollub.android.sprinteyeapp.model.local.entity;

import android.icu.util.Freezable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "lap",
foreignKeys = @ForeignKey(
        entity = RunData.class,
        parentColumns = "run_id",
        childColumns = "run_id",
        onDelete = ForeignKey.CASCADE
), indices = {
        @Index("run_id")
})
public class Lap {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "lap_id")
    public long id;
    @ColumnInfo(name = "lap_number")
    public int lapNumber;
    @ColumnInfo(name = "lap_time_ms")
    public long lapTimeMs;
    @ColumnInfo(name = "run_id")
    public long runId;
}

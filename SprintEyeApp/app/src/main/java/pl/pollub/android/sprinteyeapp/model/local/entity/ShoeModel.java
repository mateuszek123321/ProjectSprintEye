package pl.pollub.android.sprinteyeapp.model.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "shoe_model",
foreignKeys = @ForeignKey(
        entity = Athlete.class,
        parentColumns = "athlete_id",
        childColumns = "athlete_id",
        onDelete = ForeignKey.CASCADE
),
indices = {
        @Index("athlete_id")
})
public class ShoeModel {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "shoe_id")
    public long id;
    @NonNull
    @ColumnInfo(name = "shoe_name")
    public String shoeName;
    @NonNull
    @ColumnInfo(name = "shoe_type")
    public ShoeType shoeType;
    @ColumnInfo(name = "degree_of_shoe_wear")
    public int shoeWear;
    @ColumnInfo(name = "athlete_id")
    public long athleteId;
}

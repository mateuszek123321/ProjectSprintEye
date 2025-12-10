package pl.pollub.android.sprinteyeapp.model.local.entity;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "gender",
indices = {
        @Index(value = "gender_name", unique = true)
})
public class Gender {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "gender_id")
    public long id;

    @NonNull
    @ColumnInfo(name = "gender_name")
    public String genderName;
}

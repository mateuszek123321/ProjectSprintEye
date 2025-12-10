package pl.pollub.android.sprinteyeapp.model.local.entity;


import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Insert;
import androidx.room.PrimaryKey;

@Entity(tableName = "athlete",
foreignKeys = {
        @ForeignKey(
                entity = AccountUser.class,
                parentColumns = "account_user_id",
                childColumns = "account_user_id",
                onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
                entity = Gender.class,
                parentColumns = "gender_id",
                childColumns = "gender_id",
                onDelete = ForeignKey.RESTRICT)
},
indices = {
        @Index(value = "account_user_id"),
        @Index(value = "gender_id"),
        @Index(value = "athlete_nick", unique = true)
})
public class Athlete {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "athlete_id")
    public long id;
    @NonNull
    @ColumnInfo(name = "athlete_nick")
    public String nick;
    public Float weight;
    public Float height;
    public Integer age;
    @ColumnInfo(name = "created_at")
    public long createdAt;
    @ColumnInfo(name = "account_user_id")
    public long accountUserId;
    @ColumnInfo(name = "gender_id")
    public long genderId;
}

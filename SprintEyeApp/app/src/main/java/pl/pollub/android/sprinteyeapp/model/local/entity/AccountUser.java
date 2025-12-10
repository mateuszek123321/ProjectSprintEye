package pl.pollub.android.sprinteyeapp.model.local.entity;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "account_user",
foreignKeys = @ForeignKey(
        entity = Gender.class,
        parentColumns = "gender_id",
        childColumns = "gender_id",
        onDelete = ForeignKey.RESTRICT
),
indices = {
        @Index(value = "gender_id"),
        @Index(value = "email", unique = true),
        @Index(value = "user_name", unique = true)
})
public class AccountUser {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "account_user_id")
    public long id;
    @NonNull
    public String email;
    @ColumnInfo(name = "birth_date")
    public String birthDate;
    @NonNull
    @ColumnInfo(name = "user_name")
    public String userName;
    @NonNull
    @ColumnInfo(name = "password_hash")
    public String passwordHash;
    @ColumnInfo(name = "gender_id")
    public long genderId;
    @NonNull
    @ColumnInfo(name = "email_verified")
    public boolean emailVerified = false;
}


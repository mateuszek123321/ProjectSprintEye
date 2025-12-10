package pl.pollub.android.sprinteyeapp.model.local;

import android.accounts.Account;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Index;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.pollub.android.sprinteyeapp.model.local.dao.AccountUserDao;
import pl.pollub.android.sprinteyeapp.model.local.dao.AthleteDao;
import pl.pollub.android.sprinteyeapp.model.local.dao.GenderDao;
import pl.pollub.android.sprinteyeapp.model.local.dao.LapDao;
import pl.pollub.android.sprinteyeapp.model.local.dao.RunDataDao;
import pl.pollub.android.sprinteyeapp.model.local.dao.RunSessionDao;
import pl.pollub.android.sprinteyeapp.model.local.dao.ShoeModelDao;
import pl.pollub.android.sprinteyeapp.model.local.entity.AccountUser;
import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;
import pl.pollub.android.sprinteyeapp.model.local.entity.Gender;
import pl.pollub.android.sprinteyeapp.model.local.entity.Lap;
import pl.pollub.android.sprinteyeapp.model.local.entity.RunData;
import pl.pollub.android.sprinteyeapp.model.local.entity.RunSession;
import pl.pollub.android.sprinteyeapp.model.local.entity.ShoeModel;

@Database(entities = {
        AccountUser.class,
        Athlete.class,
        Gender.class,
        Lap.class,
        RunData.class,
        RunSession.class,
        ShoeModel.class
}, version = 1,
exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AccountUserDao accountUserDao();
    public abstract AthleteDao athleteDao();
    public abstract GenderDao genderDao();
    public abstract LapDao lapDao();
    public abstract RunDataDao runDataDao();
    public abstract RunSessionDao runSessionDao();
    public abstract ShoeModelDao shoeModelDao();

    private static final String DB_NAME = "sprinteye.db";
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    public static AppDatabase getInstance(Context context){
        if (INSTANCE == null){
            synchronized (AppDatabase.class){
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DB_NAME)
                            .addCallback(RoomDbCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    private static final RoomDatabase.Callback RoomDbCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db){
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                if (INSTANCE != null){
                    GenderDao genderDao = INSTANCE.genderDao();

                    Gender male = new Gender();
                    male.genderName = "Mężczyzna";
                    genderDao.insert(male);

                    Gender female = new Gender();
                    female.genderName = "Kobieta";
                    genderDao.insert(female);

                    Gender notProvide = new Gender();
                    notProvide.genderName = "Nie podaję";
                    genderDao.insert(notProvide);
            }
            });
        }
    };
}
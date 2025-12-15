package pl.pollub.android.sprinteyeapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import javax.xml.transform.Result;

import pl.pollub.android.sprinteyeapp.model.local.AppDatabase;
import pl.pollub.android.sprinteyeapp.model.local.dao.AthleteDao;
import pl.pollub.android.sprinteyeapp.model.local.dao.ShoeModelDao;
import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;
import pl.pollub.android.sprinteyeapp.model.local.entity.ShoeModel;

public class AthleteRepository {

    private final AppDatabase db;
    private final AthleteDao athleteDao;
    private final ShoeModelDao shoeModelDao;
    private final ExecutorService executor;

    public AthleteRepository(AppDatabase db) {
        this.db = db;
        this.athleteDao = db.athleteDao();
        this.shoeModelDao = db.shoeModelDao();
        this.executor = AppDatabase.databaseWriteExecutor;
    }

    public LiveData<List<Athlete>> getAthletesForUser(long userId){
        return athleteDao.observeAthletesForUser(userId);
    }

    public void insertAthleteWithShoes(Athlete athlete, List<ShoeModel> shoes){
        executor.execute(() ->{
            db.runInTransaction(() ->{
                long athleteId = athleteDao.insert(athlete);

                for (ShoeModel shoe : shoes){
                    shoe.athleteId = athleteId;
                }
                if (!shoes.isEmpty()){
                    shoeModelDao.insertAll(shoes);
                }
            });
        });
    }
    public void updateAthlete(Athlete athlete){
        executor.execute(() -> athleteDao.update(athlete));
    }

    public void updateAthleteWithShoes(Athlete athlete, List<ShoeModel> shoes){
        executor.execute(() -> {
            db.runInTransaction(() -> {
                athleteDao.update(athlete);

                shoeModelDao.deleteForAthlete(athlete.id);
                for(ShoeModel shoe : shoes){
                    shoe.athleteId = athlete.id;
                }
                if(!shoes.isEmpty()){
                    shoeModelDao.insertAll(shoes);
                }
            });
        });
    }
    public void deleteAthleteAndShoes(Athlete athlete){
        executor.execute(() -> {
            db.runInTransaction(() ->{
                athleteDao.delete(athlete);
            });
        });
    }

    public LiveData<List<ShoeModel>> getShoesForAthlete(long athleteId){
        return shoeModelDao.observeShoesForAthlete(athleteId);
    }

    public List<ShoeModel> getShoesForAthleteSync(long athleteId) {
        return shoeModelDao.getShoesForAthleteSync(athleteId);
    }

    public boolean isNickTaken(String nick, long userId, long excludeAthleteId){
        return athleteDao.countByNick(nick, userId, excludeAthleteId) > 0;
    }
}

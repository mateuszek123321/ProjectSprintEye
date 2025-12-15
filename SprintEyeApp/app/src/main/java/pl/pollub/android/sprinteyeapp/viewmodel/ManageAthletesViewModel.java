package pl.pollub.android.sprinteyeapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;

import pl.pollub.android.sprinteyeapp.model.local.AppDatabase;
import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;
import pl.pollub.android.sprinteyeapp.model.local.entity.ShoeModel;
import pl.pollub.android.sprinteyeapp.model.local.entity.ShoeType;
import pl.pollub.android.sprinteyeapp.repository.AthleteRepository;
import pl.pollub.android.sprinteyeapp.view.ManageAthletesActivity;

public class ManageAthletesViewModel extends AndroidViewModel {
    private final AthleteRepository repository;
    private final LiveData<List<Athlete>> athletes;
    private final long userId;
    public ManageAthletesViewModel(@NonNull Application application, long userId){
        super(application);
        this.userId = userId;
        AppDatabase db = AppDatabase.getInstance(application);
        repository = new AthleteRepository(db);
        athletes = repository.getAthletesForUser(userId);
    }
    public LiveData<List<Athlete>> getAthletes(){
        return athletes;
    }

    private List<ShoeModel> buildShoeList(List<String> shoeNamesShort, List<String> shoeNamesLong){
        List<ShoeModel> shoes = new ArrayList<>();

        if(shoeNamesShort != null) {
            for (String s : shoeNamesShort) {
                if (s == null || s.trim().isEmpty()) continue;

                ShoeModel shoe = new ShoeModel();
                shoe.shoeName = s.trim();
                shoe.shoeType = ShoeType.SHORT_DISTANCE;
                shoe.shoeWear = 0;
                shoes.add(shoe);
            }
        }

        if(shoeNamesLong != null) {
            for (String s : shoeNamesLong) {
                if (s == null || s.trim().isEmpty()) continue;

                ShoeModel shoe = new ShoeModel();
                shoe.shoeName = s.trim();
                shoe.shoeType = ShoeType.LONG_DISTANCE;
                shoe.shoeWear = 0;
                shoes.add(shoe);
            }
        }
        return shoes;
    }

    public void addAthlete(String name, Float weight, Float height, Integer age, long genderId, List<String> shoeNamesShort, List<String> shoeNamesLong){
        Athlete athlete = new Athlete();
        athlete.nick = name.trim();
        athlete.weight = weight;
        athlete.height = height;
        athlete.age = age;
        athlete.accountUserId = userId;
        athlete.genderId = genderId;

        List<ShoeModel> shoes = buildShoeList(shoeNamesShort, shoeNamesLong);
        repository.insertAthleteWithShoes(athlete, shoes);
    }
    public void updateAthlete(Athlete athlete){
        repository.updateAthlete(athlete);
    }

    public void updateAthleteWithShoes(Athlete athlete, List<String> shoeNamesShort, List<String> shoeNamesLong){
        List<ShoeModel> shoes = buildShoeList(shoeNamesShort, shoeNamesLong);
        repository.updateAthleteWithShoes(athlete, shoes);
    }
    public void deleteAthlete(Athlete athlete){
        repository.deleteAthleteAndShoes(athlete);
    }

    public LiveData<List<ShoeModel>> getShoesForAthlete(long athleteId) {
        return repository.getShoesForAthlete(athleteId);
    }

    public List<ShoeModel> getShoesForAthleteSync(long athleteId) {
        return repository.getShoesForAthleteSync(athleteId);
    }

    public boolean isNickTaken(String nick, long excludeAthleteId){
        return repository.isNickTaken(nick, userId, excludeAthleteId);
    }
}

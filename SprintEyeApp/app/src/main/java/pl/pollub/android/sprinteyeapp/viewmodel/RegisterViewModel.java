package pl.pollub.android.sprinteyeapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import pl.pollub.android.sprinteyeapp.model.local.AppDatabase;
import pl.pollub.android.sprinteyeapp.model.local.entity.AccountUser;
import pl.pollub.android.sprinteyeapp.model.local.entity.Athlete;
import pl.pollub.android.sprinteyeapp.model.local.entity.ShoeModel;
import pl.pollub.android.sprinteyeapp.model.response.ExistsResult;
import pl.pollub.android.sprinteyeapp.model.response.RegistrationResult;
import pl.pollub.android.sprinteyeapp.repository.AuthRepository;

public class RegisterViewModel extends AndroidViewModel {

    private final AuthRepository repository;
    private final MutableLiveData<RegistrationResult> registrationResult = new MutableLiveData<>();
    private final MutableLiveData<ExistsResult> emailExistsResult = new MutableLiveData<>();
    private final MutableLiveData<ExistsResult> userNameExistsResult = new MutableLiveData<>();

    public RegisterViewModel(@NonNull Application application){
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        repository = new AuthRepository(db);
    }
    public LiveData<RegistrationResult> getRegistrationResult(){
        return registrationResult;
    }
    public LiveData<ExistsResult> getEmailExistsResult(){
        return emailExistsResult;
    }
    public LiveData<ExistsResult> getUserNameExistsResult(){
        return userNameExistsResult;
    }
    public void registerNewUser(String email, String userName, String password, String birthDate, long genderId){
        repository.register(email, userName, password, birthDate, genderId, registrationResult);
    }
    // TODO: REMOVE FOR PRODUCTION - Local testing only
    public void registerNewUserLocally(String email, String userName, String password, String birthDate,
                                       long genderId) {
        repository.registerLocally(email, userName, password, birthDate, genderId, registrationResult);
    }
    public void isUserNameTaken(String userName){
       repository.checkUserNameExists(userName, userNameExistsResult);
    }
    public void isEmailTaken(String email){
        repository.checkEmailExists(email, emailExistsResult);
    }

}

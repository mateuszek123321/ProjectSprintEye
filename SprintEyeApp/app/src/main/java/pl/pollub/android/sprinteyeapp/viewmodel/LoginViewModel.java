package pl.pollub.android.sprinteyeapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import pl.pollub.android.sprinteyeapp.model.local.AppDatabase;
import pl.pollub.android.sprinteyeapp.model.response.ExistsResult;
import pl.pollub.android.sprinteyeapp.model.response.LoginResult;
import pl.pollub.android.sprinteyeapp.repository.AuthRepository;

public class LoginViewModel extends AndroidViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<ExistsResult> loginExistsResult = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application){
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        repository = new AuthRepository(db);
    }
    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }
    public LiveData<ExistsResult> getLoginResultExists() {
        return loginExistsResult;
    }

    public void login(String login, String password) {
        repository.login(login, password, loginResult);
    }

    // TODO: REMOVE FOR PRODUCTION - Local testing only
    public void loginLocally(String login, String password) {
        repository.loginLocally(login, password, loginResult);
    }
    public void checkLoginExists(String login){
        repository.checkLoginExists(login, loginExistsResult);
    }
}

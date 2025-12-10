package pl.pollub.android.sprinteyeapp.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import pl.pollub.android.sprinteyeapp.api.ApiClient;
import pl.pollub.android.sprinteyeapp.api.AuthApi;
import pl.pollub.android.sprinteyeapp.model.local.AppDatabase;
import pl.pollub.android.sprinteyeapp.model.local.dao.AccountUserDao;
import pl.pollub.android.sprinteyeapp.model.local.entity.AccountUser;
import pl.pollub.android.sprinteyeapp.model.request.LoginRequest;
import pl.pollub.android.sprinteyeapp.model.request.RegisterRequest;
import pl.pollub.android.sprinteyeapp.model.response.ExistsResponse;
import pl.pollub.android.sprinteyeapp.model.response.ExistsResult;
import pl.pollub.android.sprinteyeapp.model.response.LoginResponse;
import pl.pollub.android.sprinteyeapp.model.response.LoginResult;
import pl.pollub.android.sprinteyeapp.model.response.RegisterResponse;
import pl.pollub.android.sprinteyeapp.model.response.RegistrationResult;
import pl.pollub.android.sprinteyeapp.util.PasswordHasher;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AuthRepository {
    private final AuthApi authApi;
    private final AccountUserDao accountUserDao;
    private final ExecutorService dbExecutor;

    public AuthRepository(AppDatabase db){
        Retrofit retrofit = ApiClient.getClient();
        this.authApi = retrofit.create(AuthApi.class);
        this.accountUserDao = db.accountUserDao();
        this.dbExecutor = AppDatabase.databaseWriteExecutor;
    }

    // ==========================================
    // TODO: REMOVE FOR PRODUCTION - Local testing methods only
    // ==========================================

    /**
     * TEMPORARY METHOD - Local registration without server
     * Use this to test the app without backend connection
     * DELETE THIS METHOD when switching to server mode
     */
    public void registerLocally(String email, String userName, String password, String birthDate, long genderId,
                                MutableLiveData<RegistrationResult> liveData) {
        liveData.postValue(RegistrationResult.loading());

        dbExecutor.execute(() -> {
            try {
                // Check if email already exists
                AccountUser existingEmail = accountUserDao.findByEmail(email.trim());
                if (existingEmail != null) {
                    liveData.postValue(RegistrationResult.error("Email już zajęty"));
                    return;
                }

                // Check if username already exists
                AccountUser existingUserName = accountUserDao.findByUserName(userName.trim());
                if (existingUserName != null) {
                    liveData.postValue(RegistrationResult.error("Nazwa użytkownika zajęta"));
                    return;
                }

                // Hash password
                String hashedPassword = PasswordHasher.hashPassword(password);

                // Create new user
                AccountUser user = new AccountUser();
                user.email = email.trim();
                user.userName = userName.trim();
                user.passwordHash = hashedPassword;
                user.birthDate = birthDate;
                user.genderId = genderId;
                user.emailVerified = true; // In local mode, treat as verified

                accountUserDao.insert(user);
                liveData.postValue(RegistrationResult.success("Zarejestrowano lokalnie (bez serwera)"));
            } catch (Exception e) {
                liveData.postValue(RegistrationResult.error("Błąd lokalnej rejestracji: " + e.getMessage()));
            }
        });
    }

    /**
     * TEMPORARY METHOD - Local login without server
     * Use this to test the app without backend connection
     * DELETE THIS METHOD when switching to server mode
     */
    public void loginLocally(String login, String password, MutableLiveData<LoginResult> liveData) {
        liveData.postValue(LoginResult.loading());

        dbExecutor.execute(() -> {
            try {
                // Try to find user by email first
                AccountUser user = accountUserDao.findByEmail(login.trim());

                // If not found, try by username
                if (user == null) {
                    user = accountUserDao.findByUserName(login.trim());
                }

                // User not found
                if (user == null) {
                    liveData.postValue(LoginResult.error("Nieprawidłowy login lub hasło"));
                    return;
                }

                // Verify password
                if (PasswordHasher.verifyPassword(password, user.passwordHash)) {
                    liveData.postValue(LoginResult.success(user.id, user.userName, "Zalogowano lokalnie (bez serwera)"));
                } else {
                    liveData.postValue(LoginResult.error("Nieprawidłowy login lub hasło"));
                }
            } catch (Exception e) {
                liveData.postValue(LoginResult.error("Błąd lokalnego logowania: " + e.getMessage()));
            }
        });
    }

    // ==========================================
    // END OF LOCAL TESTING METHODS
    // ==========================================
    public void register(String email, String userName, String password, String birthDate, long genderId, MutableLiveData<RegistrationResult> liveData) {
        liveData.postValue(RegistrationResult.loading());

        String hashedPassword;
        try {
            hashedPassword = pl.pollub.android.sprinteyeapp.util.PasswordHasher.hashPassword(password);
        } catch (Exception e) {
            liveData.postValue(RegistrationResult.error("Błąd hashowania hasła"));
            return;
        }

        String genderCode = mapGenderIdToApi(genderId);

        RegisterRequest request = new RegisterRequest(email.trim(), userName.trim(), hashedPassword, birthDate, genderCode);

        authApi.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponse> call, @NonNull Response<RegisterResponse> response) {
                if (!response.isSuccessful()) {
                    liveData.postValue(
                            RegistrationResult.error("Błąd serwera: " + response.code())
                    );
                    return;
                }
                RegisterResponse body = response.body();
                if (body == null) {
                    liveData.postValue(
                            RegistrationResult.error("Brak odpowiedzi z serwera")
                    );
                    return;
                }
                if (!body.isSuccess()) {
                    String msg = body.getMessage() != null ? body.getMessage() : "Rejestracja nieudana";
                    liveData.postValue(RegistrationResult.error(msg));
                    return;
                }
                dbExecutor.execute(() -> {
                    try {
                        AccountUser user = new AccountUser();
                        user.email = email.trim();
                        user.userName = userName.trim();
                        user.passwordHash = hashedPassword;
                        user.birthDate = birthDate;
                        user.genderId = genderId;
                        user.emailVerified = body.isEmailVerified();

                        accountUserDao.insert(user);
                        liveData.postValue(RegistrationResult.success("Zarejestrowano pomyślnie"));
                    } catch (Exception e) {
                        liveData.postValue(RegistrationResult.error("Błąd zapisu użytkownika:" + e.getMessage()));
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponse> call, @NonNull Throwable t) {
                String message;
                if (t instanceof IOException) {
                    message = "Problem z połączeniem internetowym";
                } else {
                    message = "Nieoczekiwany błąd: " + t.getMessage();
                }
                liveData.postValue(RegistrationResult.error(message));
            }
        });
    }

    private String mapGenderIdToApi(long genderId){
        if(genderId == 1L) return "MALE";
        if(genderId == 2L) return "FEMALE";
        return "OTHER";
    }
    public void login(String login, String password, MutableLiveData<LoginResult> liveData){
        liveData.postValue(LoginResult.loading());

        LoginRequest request = new LoginRequest(login.trim(), password);

        authApi.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if(!response.isSuccessful()){
                    liveData.postValue(LoginResult.error("Błąd serwera: " + response.code()));
                    return;
                }
                LoginResponse body = response.body();
                if(body == null){
                    liveData.postValue(LoginResult.error("Brak odpowiedzi z serwera"));
                    return;
                }
                if(!body.isSuccess()){
                    String msg = body.getMessage() != null ? body.getMessage() : "Logowanie nieudane";
                    liveData.postValue(LoginResult.error(msg));
                    return;
                }
                if(!body.isEmailVerified()){
                    liveData.postValue(LoginResult.error("Brak zweryfikowanego emaila.")
                    );
                    return;
                }
                dbExecutor.execute(() -> {
                    try{
                        AccountUser localUser = accountUserDao.findByEmail(login.trim());
                        if(localUser == null){
                            localUser = accountUserDao.findByUserName(login.trim());
                        }
                        if(localUser == null){
                            localUser = new AccountUser();
                            localUser.email = body.getUserId() != null ? login.trim() : "";
                            localUser.userName = login.trim();
                            localUser.passwordHash = PasswordHasher.hashPassword(password);
                            localUser.emailVerified = true;
                            localUser.genderId = 3L;

                            long userId = accountUserDao.insert(localUser);
                            liveData.postValue(LoginResult.success(userId, localUser.userName, "Zalogowano pomyślnie"));
                        }else{
                            if(PasswordHasher.verifyPassword(password, localUser.passwordHash)){
                                if(!localUser.emailVerified && body.isEmailVerified()){
                                    localUser.emailVerified = true;
                                    accountUserDao.update(localUser);
                                }
                                liveData.postValue(LoginResult.success(localUser.id, localUser.userName, "Zalogowano pomyślnie"));
                            }else{
                                //for update password from server(in case user changes it in another device)
                                localUser.passwordHash = PasswordHasher.hashPassword(password);
                                localUser.emailVerified = body.isEmailVerified();
                                accountUserDao.update(localUser);
                                liveData.postValue(LoginResult.success(localUser.id, localUser.userName, "Zalogowano pomyślnie"));
                            }
                        }
                    }catch (Exception e){
                        liveData.postValue(LoginResult.error("Błąd zapisu w aplikacji" + e.getMessage()));
                    }
                });
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                String message;
                if(t instanceof IOException) {
                    message = "Problem z połączeniem internetowym";
                }else {
                    message = "Nieoczekiwany błąd " + t.getMessage();
                }
                liveData.postValue(LoginResult.error(message));
            }
        });
    }

    public void checkEmailExists(String email, MutableLiveData<ExistsResult> liveData){
        liveData.postValue(ExistsResult.checking());

        authApi.checkEmail(email).enqueue(new Callback<ExistsResponse>() {
            @Override
            public void onResponse(Call<ExistsResponse> call, Response<ExistsResponse> response) {
                if(!response.isSuccessful()){
                    liveData.postValue(ExistsResult.error("Błąd sprawdzania emaila"));
                    return;
                }

                ExistsResponse body = response.body();
                if(body == null){
                    liveData.postValue(ExistsResult.error("Brak odpowiedzi"));
                    return;
                }
                if(body.isExists()){
                    liveData.postValue(ExistsResult.exists("Konto z tym emailem już istnieje"));
                }
            }
            @Override
            public void onFailure(@NonNull Call<ExistsResponse> call, @NonNull Throwable t) {
                if(t instanceof IOException){
                    liveData.postValue(ExistsResult.error("Brak połączenia"));
                }else{
                    liveData.postValue(ExistsResult.error("Błąd:" + t.getMessage()));
                }
            }
        });
    }
    public void checkUserNameExists(String userName, MutableLiveData<ExistsResult> liveData){
        liveData.postValue(ExistsResult.checking());

        authApi.checkUserName(userName).enqueue(new Callback<ExistsResponse>() {
            @Override
            public void onResponse(Call<ExistsResponse> call, Response<ExistsResponse> response) {
                if(!response.isSuccessful()){
                    liveData.postValue(ExistsResult.error("Błąd sprawdzania nazwy użytkownika"));
                    return;
                }
                ExistsResponse body = response.body();

                if(body == null){
                    liveData.postValue(ExistsResult.error("Brak odpowiedzi"));
                    return;
                }

                if(body.isExists()){
                    liveData.postValue(ExistsResult.exists("Nazwa użytkownika jest zajęta"));
                }
            }

            @Override
            public void onFailure(Call<ExistsResponse> call, Throwable t) {
                if(t instanceof IOException){
                    liveData.postValue(ExistsResult.error("Brak połączenia"));
                }else{
                    liveData.postValue(ExistsResult.error("Błąd:"+t.getMessage()));
                }
            }
        });
    }

    public void checkLoginExists(String login, MutableLiveData<ExistsResult> liveData){
        liveData.postValue(ExistsResult.checking());

        authApi.checkEmail(login).enqueue(new Callback<ExistsResponse>() {
            @Override
            public void onResponse(Call<ExistsResponse> call, Response<ExistsResponse> response) {
                if(response.isSuccessful() && response.body() != null && response.body().isExists()){
                    liveData.postValue(ExistsResult.exists("Login poprawny"));
                    return;
                }

                authApi.checkUserName(login).enqueue(new Callback<ExistsResponse>() {
                    @Override
                    public void onResponse(Call<ExistsResponse> call, Response<ExistsResponse> response) {
                        if(response.isSuccessful() && response.body() != null){
                            if(response.body().isExists()){
                                liveData.postValue(ExistsResult.exists("Login poprawny"));
                            }else{
                                liveData.postValue(ExistsResult.notExists(null));
                            }
                        }else {
                            liveData.postValue(ExistsResult.error("Błąd sprawdzenia loginu"));
                        }
                    }

                    @Override
                    public void onFailure(Call<ExistsResponse> call, Throwable t) {
                        if(t instanceof IOException){
                            liveData.postValue(ExistsResult.error("Błąd połączenia"));
                        }else{
                            liveData.postValue(ExistsResult.error("Błąd"));
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<ExistsResponse> call, Throwable t) {
                if(t instanceof IOException){
                    liveData.postValue(ExistsResult.error("Brak połączenia"));
                }else {
                    liveData.postValue(ExistsResult.error("Błąd"));
                }
            }
        });
    }

}

package pl.pollub.android.sprinteyeapp.util;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {
    private static final String NAME = "SprintEyeUserSession";
    private static final String KEY_USER_ID = "logged_user_id";
    private static final String KEY_IS_LOGGED_ID = "is_logged_in";

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public UserSessionManager(Context context){
        preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }
    public void setUserLoggedIn(long userId) {
        editor.putLong(KEY_USER_ID, userId);
        editor.putBoolean(KEY_IS_LOGGED_ID, true);
        editor.apply();
    }

    public long getCurrentUserId(){
        return preferences.getLong(KEY_USER_ID, -1L);
    }
    public boolean isLoggedIn(){
        return preferences.getBoolean(KEY_IS_LOGGED_ID, false);
    }
    public void logout() {
        editor.clear();
        editor.apply();
    }
}

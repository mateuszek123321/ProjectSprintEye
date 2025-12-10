package pl.pollub.android.sprinteyeapp.model.response;

import androidx.annotation.Nullable;

public class LoginResult {
    public enum Status{
        LOADING,
        SUCCESS,
        ERROR
    }

    public final Status status;
    @Nullable
    public final String message;
    @Nullable
    public final Long localUserId;

    @Nullable
    public final String userName;

    private LoginResult(Status status, @Nullable String message, @Nullable Long localUserId, @Nullable String userName){
        this.status = status;
        this.message = message;
        this.localUserId = localUserId;
        this.userName = userName;
    }

    public static LoginResult loading() {
        return new LoginResult(Status.LOADING, null, null, null);
    }
    public static LoginResult success(@Nullable Long localUserId, @Nullable String userName, @Nullable String message) {
        return new LoginResult(Status.SUCCESS, message, localUserId, userName);
    }
    public static LoginResult error(String message) {
        return new LoginResult(Status.ERROR, message, null, null);
    }

}

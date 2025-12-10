package pl.pollub.android.sprinteyeapp.model.response;

import androidx.annotation.Nullable;

public class RegistrationResult {
    public enum Status{
        LOADING,
        SUCCESS,
        ERROR
    }
    public final Status status;
    @Nullable
    public final String message;

    private RegistrationResult(Status status, @Nullable String message){
        this.status = status;
        this.message = message;
    }
    public static RegistrationResult loading(){
        return new RegistrationResult(Status.LOADING, null);
    }

    public static RegistrationResult success(@Nullable String message) {
        return new RegistrationResult(Status.SUCCESS, message);
    }

    public static RegistrationResult error(String message) {
        return new RegistrationResult(Status.ERROR, message);
    }
}

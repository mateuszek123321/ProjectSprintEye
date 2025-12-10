package pl.pollub.android.sprinteyeapp.model.response;

import androidx.annotation.Nullable;

public class ExistsResult {
    public enum Status{
        CHECKING,
        EXISTS,
        NON_EXISTS,
        ERROR
    }
    public final Status status;
    @Nullable
    public final String message;
    private ExistsResult(Status status, @Nullable String message){
        this.status = status;
        this.message = message;
    }
    public static ExistsResult checking(){
        return new ExistsResult(Status.CHECKING, null);
    }
    public static ExistsResult exists(@Nullable String message){
        return new ExistsResult(Status.EXISTS, message);
    }
    public static ExistsResult notExists(@Nullable String message){
        return new ExistsResult(Status.NON_EXISTS, message);
    }
    public static ExistsResult error(String message){
        return new ExistsResult(Status.ERROR, message);
    }
}

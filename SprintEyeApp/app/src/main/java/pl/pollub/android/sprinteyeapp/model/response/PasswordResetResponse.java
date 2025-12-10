package pl.pollub.android.sprinteyeapp.model.response;

public class PasswordResetResponse {
    private boolean sucess;
    private String message;
    public boolean isSucess(){
        return sucess;
    }
    public String getMessage(){
        return message;
    }
}

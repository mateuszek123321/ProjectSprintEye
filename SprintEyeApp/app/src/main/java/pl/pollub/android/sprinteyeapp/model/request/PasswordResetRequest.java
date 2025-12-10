package pl.pollub.android.sprinteyeapp.model.request;

public class PasswordResetRequest {
    private String email;
    public PasswordResetRequest(String email){
        this.email = email;
    }
    public String getEmail(){
        return email;
    }
}

package pl.pollub.android.sprinteyeapp.model.response;

public class LoginResponse {
    private boolean success;
    private String message;
    private Long userId;
    private String token;
    private boolean emailVerified;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

}

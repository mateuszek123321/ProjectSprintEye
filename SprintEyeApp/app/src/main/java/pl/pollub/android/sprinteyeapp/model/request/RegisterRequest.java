package pl.pollub.android.sprinteyeapp.model.request;

public class RegisterRequest {
    private String userEmail;
    private String userName;
    private String password;
    private String birthDate;
    private String gender;

    public RegisterRequest(String userEmail,
                           String userName,
                           String password,
                           String birthDate,
                           String gender) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.password = password;
        this.birthDate = birthDate;
        this.gender = gender;
    }
    public String getUserEmail() { return userEmail; }
    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getBirthDate() { return birthDate; }
    public String getGender() { return gender; }
}

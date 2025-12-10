package pl.pollub.android.sprinteyeapp.api;

import pl.pollub.android.sprinteyeapp.model.request.LoginRequest;
import pl.pollub.android.sprinteyeapp.model.request.PasswordResetRequest;
import pl.pollub.android.sprinteyeapp.model.request.RegisterRequest;
import pl.pollub.android.sprinteyeapp.model.response.ExistsResponse;
import pl.pollub.android.sprinteyeapp.model.response.LoginResponse;
import pl.pollub.android.sprinteyeapp.model.response.RegisterResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthApi {
    @POST("api/v1/register")
    Call<RegisterResponse> register(@Body RegisterRequest registerRequest);

    @POST("api/v1/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("api/v1/check-username")
    Call<ExistsResponse> checkUserName(@Query("username") String userName);

    @GET("api/v1/check-email")
    Call<ExistsResponse> checkEmail(@Query("email") String email);

    @POST("api/v1/reset-password")
    Call<PasswordResetRequest> resetPassword(@Body PasswordResetRequest passwordResetRequest);
}

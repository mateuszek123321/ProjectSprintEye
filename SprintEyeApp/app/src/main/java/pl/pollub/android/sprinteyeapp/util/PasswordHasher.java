package pl.pollub.android.sprinteyeapp.util;


import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordHasher {
    private static final int BCRYPT_COST = 12;
    public static String hashPassword(String plainPassword){
        if(plainPassword == null || plainPassword.isEmpty()){
            throw new IllegalArgumentException("Hasło nie może być puste");
        }
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
    }
    public static boolean verifyPassword(String plainPassword, String hashedPassword){
        if(plainPassword == null || hashedPassword == null){
            return false;
        }
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}

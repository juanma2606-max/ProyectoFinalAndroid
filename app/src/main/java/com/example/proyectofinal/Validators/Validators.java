package com.example.proyectofinal.Validators;

public class Validators {

    public static boolean isRestrictedWord(String value, String[] restrictedWords) {
        if (value == null) return false;

        value = value.toLowerCase();

        for (String word : restrictedWords) {
            if (value.equals(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isStrongPassword(String password) {
        if (password == null || password.isEmpty()) return false;

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");

        return hasUpper && hasNumber;
    }

    public static boolean passwordsMatch(String pass1, String pass2) {
        if (pass1 == null || pass2 == null) return false;
        return pass1.equals(pass2);
    }

    public static boolean customValidator(String value) {
        boolean isValid = true;
        return isValid;
    }
}

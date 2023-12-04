package com.javamini02.validator;

public class ValidatorFactory {
    public static Validator getValidator(String input) {
        if (isNumeric(input)) {
            return NumericValidator.getInstance();
        } else {
            return EnglishAlphabetsValidator.getInstance();
        }
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }
}

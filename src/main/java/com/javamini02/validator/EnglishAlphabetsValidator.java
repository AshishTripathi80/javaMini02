package com.javamini02.validator;

public class EnglishAlphabetsValidator implements Validator {
    private static final EnglishAlphabetsValidator instance = new EnglishAlphabetsValidator();

    private EnglishAlphabetsValidator() {}

    public static EnglishAlphabetsValidator getInstance() {
        return instance;
    }

    @Override
    public boolean validate(String input) {
        return input.matches("^[a-zA-Z]+$");
    }
}


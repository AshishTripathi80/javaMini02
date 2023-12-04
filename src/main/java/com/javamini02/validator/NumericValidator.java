package com.javamini02.validator;

public class NumericValidator implements Validator {
    private static final NumericValidator instance = new NumericValidator();

    private NumericValidator() {}

    public static NumericValidator getInstance() {
        return instance;
    }

    @Override
    public boolean validate(String input) {
        try {
            Long.parseLong(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
